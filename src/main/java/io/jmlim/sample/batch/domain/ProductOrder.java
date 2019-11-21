package io.jmlim.sample.batch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

import javax.persistence.*;

@Alias("ProductOrder")
@ToString
@Getter
@Setter
@NoArgsConstructor
@Entity
public class ProductOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productCode;
    private String productName;
    @Column(length = 1024)
    private String productDescription;
    private Double priceEach;
    private Integer quantityOrdered;
    private Integer orderLineNumber;
}
