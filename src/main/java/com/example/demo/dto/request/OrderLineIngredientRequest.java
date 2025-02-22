package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineIngredientRequest {
  Long id;
  Long ingredientId;
  Long orderLineId;
  @Builder.Default
  int quantity = 1;

}
