package net.ahm.di.ingest.dataLoader;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.hive.HiveContext;

public class DataLoader {

	private JavaSparkContext context;
	private SQLContext hiveCtx = null;

	DataLoader(JavaSparkContext context, SQLContext hiveCtx) {
		this.context = context;
		this.hiveCtx = hiveCtx;
	}

	public DataFrame loadODSDF(String tableName) {
		DataFrame df = null;
		try {
			// prod server details

			Map<String, String> options = new HashMap<>();
			options.put("driver", "oracle.jdbc.driver.OracleDriver");
			options.put("url", "jdbc:oracle:thin:@azaupprdodsrc04:1522:azodsp4");

			options.put("dbtable", tableName);
			options.put("partitionColumn", "SUPPLIER");
			options.put("lowerBound", "178");
			options.put("upperBound", "15549");
			options.put("numPartitions", "50");
			options.put("user", "ods_stag4");
			options.put("password", "welcome");
			options.put("MaxPooledStatements", "50");
			options.put("fetchSize", "50000");

			df = hiveCtx.read().format("jdbc").options(options).load();

		} catch (Exception exe) {
			exe.printStackTrace();
			// log.error("Exception while loading the data from location");
		}
		return df;
	}

	public void loadODSInsdata() throws Exception {

		System.out.println("Loading Insurance into DF ");
		DataFrame loadADDRDF = loadODSDF("(select * from ods_stag4.t_insurance_aetna) a");
		loadADDRDF.registerTempTable("insurance");
		DataFrame addressDF = hiveCtx.sql("select 20453 AS ACCOUNTID,1 AS ODSMEMBERID,PATID AS PATIENTID,"
				+ "SUPPLIER AS AHMSUPPLIERID,RELCODE AS RELATIONSHIPTYPCD,COVGSTARTDT,"
				+ "COVGENDDT,SUPPLIER AS MASTERSUPPLIER,"
				+ "case when COVGENDDT is null then 1 else 3 end as RULEID,1 AS CREATEDBATCHID,"
				+ "1 AS UPDATEDBATCHID,NULL AS PCPID,NULL as PCPSTARTDT,NULL as PCPENDDT from insurance");
		addressDF.write().format("com.databricks.spark.csv").option("quote", null).option("inferSchema", "false")
				.mode(SaveMode.Overwrite).save("MemInsurance");
		System.out.println("Loaded Insurance into DF ");

	}

	public void loadMemInsData() throws Exception {
		System.out.println("Loading loadMemInsData into DF ");
		DataFrame memInsDF = readDataFromHBase(
				"select ACCOUNTID,DIPATIENTID,ODSMEMBERID,PATIENTID,RELATIONSHIPTYPCD from PROD_AHM_DI.DIHB_MEMBERDETAILS");
		memInsDF.registerTempTable("MemDetails");
		System.out.println("Loading MemDetails into DF ");
		String qry = "select m.DIPATIENTID,i.ACCOUNTID,m.ODSMEMBERID,i.PATIENTID,i.AHMSUPPLIERID,"
				+ "i.RELATIONSHIPTYPCD,to_date(from_unixtime(UNIX_TIMESTAMP(i.COVGSTARTDT,'yyyy-MM-dd'))) as COVGSTARTDT,"
				+ "to_date(from_unixtime(UNIX_TIMESTAMP(i.COVGENDDT,'yyyy-MM-dd'))) as COVGENDDT,"
				+ "i.MASTERSUPPLIER AS MASTERSUPPLIERID ,cast(i.RULEID as int) as RULEID,i.CREATEDBATCHID,i.UPDATEDBATCHID,i.PCPID,"
				+ "to_date(from_unixtime(UNIX_TIMESTAMP(i.PCPSTARTDT,'yyyy-MM-dd'))) as PCPSTARTDT,"
				+ "to_date(from_unixtime(UNIX_TIMESTAMP(i.PCPENDDT,'yyyy-MM-dd'))) as PCPENDDT,"
				+ "CURRENT_DATE() AS INSERTDT,CURRENT_DATE() AS UPDATEDT"
				+ " from free_dev_enc.insCoverage i, MemDetails m where m.PATIENTID = i.PATIENTID AND m.RELATIONSHIPTYPCD=i.RELATIONSHIPTYPCD";
		DataFrame sql = hiveCtx.sql(qry).repartition(50);
		writeIntoHBase(sql, "PROD_AHM_DI.DIHB_MEMBERINSURANCE");
	}

	private DataFrame readDataFromHBase(String tableName) {
		DataFrame load = null;
		try {
			System.out.println("Reqding data into Phoenix ");
			Map<String, String> options = new HashMap<>();
			options.put("driver", "org.apache.phoenix.jdbc.PhoenixDriver");
			options.put("url",
					"jdbc:phoenix:xhadhbasem1p.aetna.com,xhadhivem1p.aetna.com,xhadnmgrm1p.aetna.com,xhadnnm1p.aetna.com,xhadnnm2p.aetna.com:2181:/hbase-secure");
			options.put("dbtable", "( " + tableName + " )");
			options.put("lowerBound", "7189");
			options.put("partitionColumn", "ODSMEMBERID");
			// options.put("fetchsize", "10000");
			options.put("upperBound", "398991777");
			options.put("numPartitions", "50");
			load = hiveCtx.read().format("jdbc").options(options).load();
			System.out.println("Done... ");
		} catch (Exception exe) {
			exe.printStackTrace();
		}
		return load;
	}

	private void writeIntoHBase(DataFrame withPKColumn, String tblName) {
		try {
			System.out.println("Writting data into Phoenix ");

			withPKColumn.write().format("org.apache.phoenix.spark").mode(SaveMode.Overwrite)
					.options(com.google.common.collect.ImmutableMap.of("driver",
							"org.apache.phoenix.jdbc.PhoenixDriver", "zkUrl",
							"jdbc:phoenix:xhadhbasem1p.aetna.com,xhadhivem1p.aetna.com,xhadnmgrm1p.aetna.com,xhadnnm1p.aetna.com,xhadnnm2p.aetna.com:2181:/hbase-secure",
							"table", tblName))
					.save();
			System.out.println("Done... ");
		} catch (Exception exe) {
			exe.printStackTrace();
		} finally {
			context.close();
		}
	}

	public static void main(String[] args) {
		JavaSparkContext context = null;

		try {
			SparkConf conf = new SparkConf();
			conf.setAppName("Bulk Member insurance Process");
			context = new JavaSparkContext(conf);

			HiveContext hiveCtx = new HiveContext(context);

			Configuration con_f = hiveCtx.sparkContext().hadoopConfiguration();

			hiveCtx.sql("create temporary function accessdata as 'com.aetna.ise.voltage.hive.udf.AccessData'");
			hiveCtx.sql("create temporary function protectdata as 'com.aetna.ise.voltage.hive.udf.ProtectData'");

			con_f.set("hbase.client.pause", "1000");

			con_f.set("hbase.rpc.timeout", "1200000");
			con_f.set("hbase.client.retries.number", "3");
			con_f.set("zookeeper.recovery.retry", "1");
			con_f.set("hbase.client.operation.timeout", "30000");
			con_f.set("hbase.client.scanner.timeout.period", "1200000");
			con_f.set("phoenix.query.timeoutMs", "1800000");

			String applicationId = context.sc().applicationId();
			System.out.println("Application Started with applicationId : " + applicationId);

			DataLoader job = new DataLoader(context, hiveCtx);
			// job.loadODSInsdata();
			job.loadMemInsData();
		} catch (Exception exe) {
			exe.printStackTrace();
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

}
