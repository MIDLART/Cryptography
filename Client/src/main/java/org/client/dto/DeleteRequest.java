package org.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteRequest {
  private String sender;
  private String recipient;
  private boolean forBoth;

  public boolean getForBoth() {
    return forBoth;
  }
}
