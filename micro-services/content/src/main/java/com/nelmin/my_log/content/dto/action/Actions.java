package com.nelmin.my_log.content.dto.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Actions {
    private Boolean canEdit = false;
    private Boolean canDelete = false;
    private Boolean canPublish = false;
    private Boolean canUnpublish = false;
    private Boolean canSubscribe = false;
    private Boolean canUnsubscribe = false;
    private Boolean canReport = false;
    private Boolean canReact = false;
}
