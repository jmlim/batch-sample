package io.jmlim.sample.batch.mapper;

import io.jmlim.sample.batch.domain.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductOrderMapper {

    List<ProductOrder> findProductOrder(@Param("orderLineNumber") Integer orderLineNumber);

    int insertProductOrder(ProductOrder productOrder);

    int deleteProductOrder(@Param("orderLineNumber") Integer orderLineNumber);
}
