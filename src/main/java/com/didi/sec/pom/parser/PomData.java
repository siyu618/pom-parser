package com.didi.sec.pom.parser;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PomData {
    private String groupId;
    private String artifactId;
    private String version;
    @Builder.Default private String packaging = "jar";
}
