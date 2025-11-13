package com.codeit.session.mapper;


import com.codeit.session.dto.post.PostDto;
import com.codeit.session.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PostMapper {

    @Mapping(source = "author", target = "author")
    PostDto toDto(Post post);
}
