from typing import Optional

from pydantic import BaseModel, ConfigDict, Field, model_validator


class MemberCreate(BaseModel):
    first_name: str = Field(..., min_length=1)
    last_name: str = Field(..., min_length=1)
    relation_type: Optional[str] = Field(None, pattern="^(parent|child)$")
    related_member_id: Optional[int] = None
    image_base64: Optional[str] = Field(None, max_length=3_000_000)

    @model_validator(mode="after")
    def validate_relation_fields(self):
        has_relation_type = self.relation_type is not None
        has_related_member = self.related_member_id is not None
        if has_relation_type != has_related_member:
            raise ValueError("relation_type and related_member_id must be provided together")
        return self


class MemberOut(BaseModel):
    id: int
    first_name: str
    last_name: str
    parent_id: Optional[int] = None
    image_base64: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)
