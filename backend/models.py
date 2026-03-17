from sqlalchemy import Column, Integer, String, ForeignKey, LargeBinary
from sqlalchemy.orm import relationship

from database import Base


class Member(Base):
    __tablename__ = "members"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String(100), nullable=False)
    last_name = Column(String(100), nullable=False)
    parent_id = Column(Integer, ForeignKey("members.id"), nullable=True, index=True)
    image_data = Column(LargeBinary, nullable=True)

    parent = relationship("Member", remote_side=[id], backref="children")
