package org.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.client.models.ChatSettings;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationRequest {
  private ChatSettings chatSettings;
  private Invitation invitation;
}
