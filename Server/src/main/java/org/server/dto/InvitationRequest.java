package org.server.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationRequest {
  private String recipient;
  private Invitation invitation;
}
