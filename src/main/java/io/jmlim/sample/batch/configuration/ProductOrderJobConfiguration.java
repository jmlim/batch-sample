package io.jmlim.sample.batch.configuration;

import io.jmlim.sample.batch.domain.ProductOrder;
import io.jmlim.sample.batch.mapper.ProductOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 개선필요.. 스프링 배치는 초보라..
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProductOrderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    // private final DataSource dataSource; // DataSource DI
    private final SqlSessionFactory sqlSessionFactory;
    private final ProductOrderMapper productOrderMapper;

    private static final int chunkSize = 10;

    /**
     * --job.name=productOrderJob version=1
     * --job.name=productOrderJob version=1 ORDER_LINE_NUMBER=3
     * @return
     */
    @Bean
    public Job productOrderJob() {
        return jobBuilderFactory.get("productOrderJob")
                .listener(listener())
                .start(productOrderStep())
                .build();
    }

    @Bean
    public Step productOrderStep() {
        return stepBuilderFactory.get("productOrderStep")
                .<ProductOrder, ProductOrder>chunk(chunkSize)
                .reader(productOrderMybatis(null))
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                System.out.println("before job");
                productOrderMapper.deleteProductOrder(1);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                System.out.println("after job");
            }
        };
    }

    /**
     * 참고..
     * https://github.com/developma/spring-batch-example/blob/master/src/main/java/com/example/batch/config/BatchConfig.java
     *
     * @return
     */
    @StepScope
    @Bean
    public MyBatisCursorItemReader productOrderMybatis(@Value("#{@paramValues}")
                                                                   Map<String, Object> paramValues) {
        /*final MyBatisCursorItemReader<ProductOrder> reader = new MyBatisCursorItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("io.jmlim.sample.batch.mapper.ProductOrderMapper.findProductOrder");
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("orderLineNumber", "1");
        reader.setParameterValues(objectObjectHashMap);
        return reader;*/
        return new MyBatisCursorItemReaderBuilder<ProductOrder>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("io.jmlim.sample.batch.mapper.ProductOrderMapper.findProductOrder")
                .parameterValues(paramValues)
                .build();
    }

    @StepScope
    @Bean
    public Map<String, Object> paramValues(
            @Value("#{jobParameters[ORDER_LINE_NUMBER]}") Integer orderLineNumber) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderLineNumber", orderLineNumber);
        return map;
    }

  /*  @Bean
    public JdbcCursorItemReader<ProductOrder> productOrder() {
        return new JdbcCursorItemReaderBuilder<ProductOrder>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(ProductOrder.class))
                .sql("select \n" +
                        "\tp.productCode, \n" +
                        "\tp.productName, \n" +
                        "\tp.productDescription, \n" +
                        "\tod.priceEach, \n" +
                        "\tod.quantityOrdered, \n" +
                        "\tod.orderLineNumber\n" +
                        "from products p inner join orderdetails od\n" +
                        "on p.productCode = od.productCode\n" +
                        "where od.orderLineNumber = '9'")
                .name("productOrder")
                .build();
    }*/

    /**
     * reader에서 넘어온 데이터를 하나씩 출력하는 writer
     */
    private ItemWriter<ProductOrder> jdbcCursorItemWriter() {
        return list -> {
            for (ProductOrder productOrder : list) {
                log.info("Current ProductOrder={}", productOrder);
                productOrderMapper.insertProductOrder(productOrder);
            }
        };
    }
}
