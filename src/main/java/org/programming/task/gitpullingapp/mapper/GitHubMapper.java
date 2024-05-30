package org.programming.task.gitpullingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.outgoing.dto.BranchApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GitHubMapper {

    @Mapping(source = "commit.sha", target = "lastCommit")
    @Mapping(source = "name", target = "branchName")
    BranchDto toBranchDto(BranchApiResponse branchApiResponse);

    @Mapping(source = "repositoryApiResponse.name", target = "repositoryName")
    @Mapping(source = "repositoryApiResponse.owner.login", target = "ownerLogin")
    UserRepositoryDto toUserRepositoryDto(RepositoryApiResponse repositoryApiResponse, List<BranchDto> branches);

}
