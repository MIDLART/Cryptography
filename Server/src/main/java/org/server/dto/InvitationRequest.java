package org.server.dto;

import lombok.*;
import org.server.models.ChatSettings;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationRequest {
  private ChatSettings chatSettings;
  private Invitation invitation;
}
