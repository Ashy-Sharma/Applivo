package com.projects.applivo.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionMessage {

    private InteractionType type;

    private Integer x;

    private Integer y;

    private Integer x2;

    private Integer y2;

    private Integer duration;

    private String text;

    private Integer keyCode;

}
