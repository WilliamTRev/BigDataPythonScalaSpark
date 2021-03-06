package com

import org.apache.spark.sql.SparkSession

object hivetestP1{
  def main(args: Array[String]): Unit = {
    // create a spark session
    // for Windows
    System.setProperty("hadoop.home.dir", "C:\\Hadoop3")
    val spark = SparkSession
      .builder
      .appName("hello hive")
      .config("spark.master", "local")
      .enableHiveSupport()
      .getOrCreate()
    println("created spark session")
    spark.sparkContext.setLogLevel("ERROR")
    spark.sql("DROP table IF EXISTS BevA")
    spark.sql("create table IF NOT EXISTS BevA(Beverage String,BranchID String) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_BranchA.txt' INTO TABLE BevA")
    spark.sql("SELECT Count(*) AS TOTALCOUNT FROM BevA").show()
    spark.sql("SELECT Count(*) AS NumBranch2BevAFile FROM BevA WHERE BevA.BranchID='Branch2'").show()
    //spark.sql("SELECT * FROM BevA").show()

    //spark.sql("DROP table IF EXISTS BevB")
    spark.sql("create table IF NOT EXISTS BevB(Beverage String,BranchID String) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_BranchB.txt' INTO TABLE BevB")
    //spark.sql("SELECT * FROM BevB").show()

    spark.sql("DROP table IF EXISTS BevC")
    spark.sql("create table IF NOT EXISTS BevC(Beverage String,BranchID String) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_BranchC.txt' INTO TABLE BevC")
    //spark.sql("SELECT * FROM BevC").show()

    spark.sql("DROP table IF EXISTS BevAll")
    spark.sql("CREATE TABLE BevAll(Beverage STRING,BranchID STRING)row format delimited fields terminated by ','")
    spark.sql("INSERT INTO TABLE BevAll (SELECT * FROM BevA UNION ALL SELECT * FROM BevB UNION ALL SELECT * FROM BevC)")
    //spark.sql("SELECT * FROM BevAll").show()

    spark.sql("DROP table IF EXISTS ConsA")
    spark.sql("create table IF NOT EXISTS ConsA(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_ConscountA.txt' INTO TABLE ConsA")
    //spark.sql("SELECT Count(*) AS TOTALCOUNT FROM ConsA").show()
    //spark.sql("SELECT * FROM ConsA").show()

    spark.sql("DROP table IF EXISTS ConsB")
    spark.sql("create table IF NOT EXISTS ConsB(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_ConscountB.txt' INTO TABLE ConsB")
    //spark.sql("SELECT * FROM ConsB").show()

    spark.sql("DROP table IF EXISTS ConsC")
    spark.sql("create table IF NOT EXISTS ConsC(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_ConscountC.txt' INTO TABLE ConsC")
    spark.sql("SELECT * FROM ConsC").show()

    spark.sql("DROP TABLE IF EXISTS ConsAll");
    spark.sql("create table IF NOT EXISTS ConsAll(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("INSERT INTO TABLE ConsAll (SELECT * FROM ConsA UNION ALL SELECT * FROM ConsB UNION ALL SELECT * FROM ConsC)");
    //spark.sql("SELECT * FROM ConsAll").show()

    //Problem 1
    //What is the total number of consumers for Branch1?
    //What is the total number of consumers for the Branch2?
    println("Problem 1 What is the total number of consumers for Branch1? What is the number of consumers for the Branch2? ")
    spark.sql(s"DROP TABLE IF EXISTS TotalCons");
    spark.sql("CREATE TABLE IF NOT EXISTS TotalCons(BranchID STRING, TotalConsumers INT) row format delimited fields terminated by ','");
    spark.sql("INSERT INTO TABLE TotalCons(SELECT BranchID, sum(ConsAll.Consumed) FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) GROUP BY BevAll.BranchID ORDER BY BevAll.BranchID)");
    //Problem 1
    spark.sql("SELECT * FROM TotalCons").show()

    //Problem 2
    //--What is the most consumed beverage on Branch1
    println("Problem 2 What is the most consumed beverage on Branch1")
    spark.sql("SELECT BranchID, sum(ConsAll.Consumed),BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch1' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) DESC").show()
    //--What is the least consumed beverage on Branch2
    //--What is the Average (median) consumed beverage of Branch2
    //spark.sql(sqlText="SELECT BranchID, ROW_NUMBER() OVER (ORDER BY sum(ConsAll.Consumed)) AS row_num,sum(ConsAll.Consumed),BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) DESC")
    println("Problem 2 What is the least consumed beverage on Branch2")
    spark.sql("SELECT BranchID,sum(ConsAll.Consumed), BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) DESC").show()
    println("least consumed")
    spark.sql("SELECT BranchID,sum(ConsAll.Consumed), BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) ASC LIMIT 1").show()
    //median shop above is number 26 or Small Latte with 93184
    println("average (median) consumed")
    spark.sql("SELECT BranchID,sum(ConsAll.Consumed), BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' AND BevAll.Beverage='SMALL_LATTE' GROUP BY BevAll.BranchID,BevAll.Beverage  ORDER BY sum(ConsAll.Consumed) DESC").show()

    //Problem 3
    //What are the beverages available on Branch10, Branch8, and Branch1?
    println("Problem 3 What are the beverages available on Branch10, Branch8, and Branch1?")
    spark.sql("SELECT BevAll.beverage, BevAll.BRANCHID FROM BevAll WHERE BRANCHID='Branch1' or BRANCHID='Branch8' or BRANCHID='Branch10' ORDER BY BevAll.beverage").show()
    //--what are the common beverages available in Branch4,Branch7?
    println("Problem 3 What are the common beverages available in Branch4,Branch7?")
    spark.sql("select BevAll.beverage from BevAll where BevAll.BRANCHID = 'Branch4' and BevAll.beverage in (select BevAll.beverage from BevAll where BevAll.BRANCHID = 'Branch7' GROUP BY BevAll.beverage)").show()

    //Problem 4
    println("View for Problem 4")
    spark.sql("DROP VIEW IF EXISTS SampView");
    spark.sql("Create VIEW SampView AS select BevAll.beverage from BevAll where BevAll.BRANCHID = 'Branch4' and BevAll.beverage in (select BevAll.beverage from BevAll where BevAll.BRANCHID = 'Branch7') GROUP BY BevAll.beverage")
    spark.sql("SELECT * FROM SampView").show()

    println("Problem 4 Partition")
    spark.sql("DROP TABLE IF EXISTS Part4")
    spark.sql("SET hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql("create table IF NOT EXISTS Part4(beverage STRING) PARTITIONED by (branchid STRING) row format delimited fields terminated by ',' ")
    spark.sql("INSERT INTO TABLE Part4(SELECT BevAll.beverage, BevAll.BRANCHID FROM BevAll WHERE BRANCHID='Branch1' or BRANCHID='Branch8' or BRANCHID='Branch10') ")
    spark.sql("show partitions Part4").show()

    //Problem 5
    println("Problem 5 properties")
    spark.sql("ALTER TABLE Part4 SET TBLPROPERTIES ('note'='this is a note')")
    spark.sql("ALTER TABLE Part4 SET TBLPROPERTIES ('comment'='this is a comment')")
    spark.sql("Show tblproperties Part4").show()

    //Problem 6
    println("Problem 6")
    spark.sql("DROP TABLE IF EXISTS Tab62");
    spark.sql("CREATE TABLE Tab62(BranchID STRING, TotalConsumers INT) STORED AS ORC ")
    spark.sql("INSERT INTO TABLE Tab62(SELECT * FROM TotalCons)")
    spark.sql("SELECT * FROM Tab62 ").show()
    spark.sql("SELECT * FROM Tab62 WHERE BRANCHID !='Branch2' ").show()

   }
}

