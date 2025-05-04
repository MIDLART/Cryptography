package org.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
  private User first;

  @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
  private User second;
}
