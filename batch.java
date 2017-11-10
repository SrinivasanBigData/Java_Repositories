package hello.sample;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.ColumnMapItemPreparedStatementSetter;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;

public class Sample {
	//https://stackoverflow.com/questions/24233821/skippable-exception-classes-for-spring-batch-with-java-based-configuration
//https://stackoverflow.com/questions/40996321/retry-not-working-with-faulttolerantstepbuilder
	@Bean
	public JdbcPagingItemReader<Customer> pagingItemReader() {
		/*JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setRowMapper(new ColumnMapRowMapper());
		reader.setSql(getSqlQueryForTable(tableName));
		reader.open(executionContext);
		return reader;*/
		JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

		reader.setDataSource(null);
		reader.setFetchSize(10);
		reader.setRowMapper(new CustomerRowMapper());

		MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
		queryProvider.setSelectClause("id, firstName, lastName, birthdate");
		queryProvider.setFromClause("from customer");

		Map<String, Order> sortKeys = new HashMap<>(1);

		sortKeys.put("id", Order.ASCENDING);

		queryProvider.setSortKeys(sortKeys);

		reader.setQueryProvider(queryProvider);

		return reader;
	}

	@Bean
	public JdbcBatchItemWriter<Map<String, Object>> customerItemWriter() {
		JdbcBatchItemWriter<Map<String, Object>> itemWriter = new JdbcBatchItemWriter<>();

		itemWriter.setDataSource(null);
		itemWriter.setSql("INSERT INTO CUSTOMER VALUES (:id, :firstName, :lastName, :birthdate)");
		itemWriter.setItemPreparedStatementSetter(new ColumnMapItemPreparedStatementSetter());
		itemWriter.afterPropertiesSet();

		return itemWriter;
	}
}
