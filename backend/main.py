import base64
import binascii
import os
from typing import List

from fastapi import Depends, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from database import Base, SessionLocal, engine
from models import Member
from schemas import MemberCreate, MemberOut

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Family Tree API")

MAX_IMAGE_BYTES = int(os.getenv("MAX_IMAGE_BYTES", str(2 * 1024 * 1024)))

raw_origins = os.getenv("CORS_ORIGINS", "http://localhost:3000,http://10.0.2.2:8000")
allowed_origins = [origin.strip() for origin in raw_origins.split(",") if origin.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["Authorization", "Content-Type"],
)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def encode_image(image_data: bytes | None) -> str | None:
    if image_data is None:
        return None
    return base64.b64encode(image_data).decode("utf-8")


def decode_image(image_base64: str | None) -> bytes | None:
    if not image_base64:
        return None
    try:
        decoded = base64.b64decode(image_base64.encode("utf-8"), validate=True)
    except (binascii.Error, ValueError) as exc:
        raise HTTPException(status_code=422, detail="Invalid image_base64 payload") from exc
    if len(decoded) > MAX_IMAGE_BYTES:
        raise HTTPException(status_code=413, detail=f"Image too large. Max {MAX_IMAGE_BYTES} bytes")
    return decoded


@app.get("/")
def root() -> dict[str, str]:
    return {"message": "Family Tree API is running", "docs": "/docs"}


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/members", response_model=List[MemberOut])
def list_members(db: Session = Depends(get_db)) -> List[MemberOut]:
    members = db.query(Member).order_by(Member.id).all()
    return [
        MemberOut(
            id=member.id,
            first_name=member.first_name,
            last_name=member.last_name,
            parent_id=member.parent_id,
            image_base64=encode_image(member.image_data),
        )
        for member in members
    ]


@app.get("/members/{member_id}", response_model=MemberOut)
def get_member(member_id: int, db: Session = Depends(get_db)) -> MemberOut:
    member = db.query(Member).filter(Member.id == member_id).first()
    if member is None:
        raise HTTPException(status_code=404, detail="Member not found")
    return MemberOut(
        id=member.id,
        first_name=member.first_name,
        last_name=member.last_name,
        parent_id=member.parent_id,
        image_base64=encode_image(member.image_data),
    )


@app.post("/members", response_model=MemberOut)
def create_member(payload: MemberCreate, db: Session = Depends(get_db)) -> MemberOut:
    try:
        image_data = decode_image(payload.image_base64)
        new_member = Member(
            first_name=payload.first_name,
            last_name=payload.last_name,
            parent_id=None,
            image_data=image_data,
        )
        db.add(new_member)
        db.flush()

        if payload.relation_type and payload.related_member_id is not None:
            related = db.query(Member).filter(Member.id == payload.related_member_id).first()
            if related is None:
                raise HTTPException(status_code=404, detail="Related member not found")

            if payload.relation_type == "child":
                new_member.parent_id = related.id
            elif payload.relation_type == "parent":
                related.parent_id = new_member.id

        db.commit()
        db.refresh(new_member)

        return MemberOut(
            id=new_member.id,
            first_name=new_member.first_name,
            last_name=new_member.last_name,
            parent_id=new_member.parent_id,
            image_base64=encode_image(new_member.image_data),
        )
    except HTTPException:
        db.rollback()
        raise
    except Exception as exc:
        db.rollback()
        raise HTTPException(status_code=500, detail="Internal server error") from exc
