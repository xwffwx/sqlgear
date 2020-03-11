# SQLGear

### 介绍
SQLGear是用java语言实现的一个用于构建动态SQL并操作关系数据库的工具，基于[apache-dbutils](https://commons.apache.org/proper/commons-dbutils/)完成，并在其基础上进一步封装，内建了逻辑处理功能，使得直接在java代码中编写动态SQL更为简单方便，既避免了直接应用jdbc接口拼接字符串的繁琐，也不用像Mybatis等工具那样需要建立额外的xml和接口文件，提高了开发效率。

### 例子
例1：一个简单的动态查询，其中**dataSource**是数据源，**table**是表名，**smap**是[SQLMap类型]()(**sqlgear**封装的Map对象)的参数，**whereWithMap**函数会根据**smap**中的字段和值动态构建查询条件，条件之间用AND连接
```javas
SQLRunner dao = new SQLRunner(dataSource);
SQLMap smap = new SQLMap().WHERE(
	,"psn_phone", "=", phone
	,"psn_type", "<>", ptype
);
List<Map<String, Object>> lst = dao.query(null, "select * from", table, whereWithMap(smap));
```
等价于下面的SQL
```sql
select * from :table where psn_phone = :phone and psn_type <> :ptype;
```

例2：在前面例子的基础上，增加逻辑处理能力（类似于Mybatis里的**&lt;IF&gt;**标签），变量**customOrder**指定排序字段名，如为null按默认字段psn_name排序，变量**pageNo**是页码，大于0表示需要分页，**pageSize**是每页的长度。函数**V**表示取值，会根据参数值的类型进行相应转换放入生成的SQL语句，并生成占位符?。
```java
List<Map<String, Object>> lst = dao.query(null,
	"SELECT * FROM t_person", whereWithMap(smap),
	"ORDER BY",
	IFElse(customOrder == null,
		"psn_name",
		customOrder
	),
	IF(pageNo > 0,
		"LIMIT", V((pageNo-1)*pageSize), ",", V(pageSize)
	)
);
```
例3：下面的例子演示了WHERE条件中AND和OR的混合使用，**WHERE**函数用于构造WHERE子句，使用AND连接条件，**OR**函数构造一个两端用()包围的，内部用OR进行连接的查询条件，**L**函数表示连接，用于构造**WHERE**和**OR**中的子条件。
```java
List<Map<String, Object>> lst = dao.query(null,
	"SELECT * FROM t_person",
	WHERE(
		L("psn_name like", V(name+"%")),
		L("psn_type='personType_joiner'"),
		OR(
			L("(psn_org =", V(org),
			L("id IN", V(ids))
		)
	)
);
```
等价于SQL
```sql
SELECT * FROM t_person
WHERE psn_name like :name% AND psn_type='personType_joiner'
	AND ( psn_org = :org OR id IN :ids );
```

### 组成结构
------------
SQLGear的结构非常简单，由以下三部分组成。
1. Map工具类：
CommonMap、SQLMap类实现了java的Map接口。其中SQLMap用于实现对SQL语句的简单封装。
2. SQL工具类：
SQL类的作用和SQLMap类类似，也用于构造SQL语句，但该类提供了一系列封装工具，表达能力更强。
3. SQL执行类：
包含SQLRunner，SpringSQL类，SQLRunner类是对QueryRunner（请参考[apache-dbutils](https://commons.apache.org/proper/commons-dbutils/)）的封装。负责执行SQL语句。SpringSQL类继承了SQLRunner类，用于和Spring事物管理的集成。

### 类参考
------------
#### 1. SQLRunner
该类是SQL语句的执行类，执行由工具类(SQLMap,SQL类)所代表的SQL语句。该类有以下三种构造方式。

| 方法名 | 功能说明 |
| ------------ | ------------ |
| SQLRunner() | 默认构造，在执行SQL语句时指定Connection连接对象，用完后不需要调用Close释放。 |
| SQLRunner(DataSource) | 用DataSource数据源构造时，会自动从DataSource获取数据库连接，并自动开启事务(SET AUTOCOMMIT=FALSE)。用完SQLRunner之后，请调用该类的Close方法保证连接的释放，否则可能会导致连接泄露。 |
| SQLRunner(Connection) | 用Connection连接对象构造，不会自动开启事务，用完后也不需要调用Close。 |

SQLRunner主要有以下几种方法

| 方法名 | 功能说明 |
| ------------ | ------------ |
| query | 执行SQL查询 |
| update | 执行SQL插入或更新 |
| commit | 提交 |
| rollback | 回滚 |
| close | 关闭并释放资源 |

##### 1.1 query方法
该方法有以下几种重载实现。
```java
//使用SQLMap对象查询
List<Map<String, Object>> query(Connection cnt, Map<String, Object> map)
//下面两个方法都是使用SQL对象查询，参数Object... sqlA会自动转变为SQL对象
List<Map<String, Object>> query(Connection cnt, Object... sqlA)
List<Map<String, Object>> query(Connection cnt, SQL sql)
```
这三个函数中的第一个参数都是**Connection**对象，表示数据库连接。如果SQLRunner构造时提供了数据源的话，可写成null，表示从数据源中获取连接。
其中，Map对象查询(第一种)用于查询条件不固定，但查询逻辑简单的情况，比如字段‘名称’，‘编号’，‘年龄’等都可以用来查询员工信息，查询时可进行任意组合，多个条件之间默认用AND进行连接。
SQL对象查询(第二、三种)要更灵活，功能更强，但要把查询逻辑考虑周全并完整写出，用于构造比较复杂的查询。
例子：分别用三种方式实现对表t_person(psn_id, psn_name, psn_age, psn_phone)实现动态条件组合查询。
```java
//第一种方法，注意和本文最先讲的例子的区别，其中SELECT用于选择列，默认选择所有列"*"
SQLRunner dao = new SQLRunner(dataSource);
SQLMap smap = new SQLMap().SELECT("*").FROM("t_person").WHERE(
	,"psn_phone", "=", phone
	,"psn_type", "<>", ptype
);
List<Map<String, Object>> lst = dao.query(null, smap);

//第二种方法
//SQL对象查询实现以上相同的效果，需要把所有条件写出，然后根据实际情况进行判断
//其中containsWhere用于检查SQLMap中是否包含参数指定列的查询条件，getWhereValue取指定列的值
List<Map<String, Object>> lst = dao.query(null, "SELECT * FROM t_person",
	WHERE(
		IF(smap.containsWhere("id"), "psn_id =", V(smap.getWhereValue("id"))),
		IF(smap.containsWhere("name"), "psn_name =", V(smap.getWhereValue("name"))),
		IF(smap.containsWhere("age"), "psn_age =", V(smap.getWhereValue("age"))),
		IF(smap.containsWhere("ptype"), "psn_type =", V(smap.getWhereValue("ptype"))),
		IF(smap.containsWhere("phone"), "psn_phone =", V(smap.getWhereValue(phone)))
	)
);

//第三种方法，本质上只是把第二种换了个写法
SQL sql = new SQL("SELECT * FROM t_person",
	WHERE(
		IF(smap.containsWhere("id"), "psn_id =", V(smap.getWhereValue("id"))),
		IF(smap.containsWhere("name"), "psn_name =", V(smap.getWhereValue("name"))),
		IF(smap.containsWhere("age"), "psn_age =", V(smap.getWhereValue("age"))),
		IF(smap.containsWhere("ptype"), "psn_type =", V(smap.getWhereValue("ptype"))),
		IF(smap.containsWhere("phone"), "psn_phone =", V(smap.getWhereValue(phone)))
	)
);
List<Map<String, Object>> lst = dao.query(null, sql);

//假如查询的比较的方式(是=,<>还是like等等)也是动态的，可以这样写
//其中getWhereOP用于获取参数指定的列的查询比较方式
List<Map<String, Object>> lst = dao.query(null, "SELECT * FROM t_person",
	WHERE(
		IF(smap.containsWhere("id"), "psn_id", smap.getWhereOP("id"), V(smap.getWhereValue("id"))),
		IF(smap.containsWhere("name"), "psn_name", smap.getWhereOP("name"), V(smap.getWhereValue("name"))),
		IF(smap.containsWhere("age"), "psn_age", smap.getWhereOP("age"), V(smap.getWhereValue("age"))),
		IF(smap.containsWhere("ptype"), "psn_type", smap.getWhereOP("ptype"), V(smap.getWhereValue("ptype"))),
		IF(smap.containsWhere("phone"), "psn_phone", smap.getWhereOP("id"), V(smap.getWhereValue(phone)))
	)
);
```
query方法返回的是一个数据集(List&lt;Map&lt;String, Object&gt;&gt;)，有时我们只是简单查询一条记录，或者只是简单查询一个字段，可用如下方法

| 方法名 | 说明 |
| ------------ | ------------ |
| queryOne | 返回值类型Map&lt;String, Object&gt;，如果查询到多条结果，也只会返回第一条 |
| queryValue | 返回值类型Object，查询第一条结果中的第一个字段 |
这两个方法的参数和query方法是一样的

##### 1.2 update方法
update方法用于执行对数据库进行修改的SQL，返回值为修改的记录条数，参数和query方法是相同的，分别支持SQLMap对象参数和SQL对象参数。
update不仅可以执行UPDATE和INSERT命令，也可以执行一些其它对数据库进行修改的命令，如创建表。
```java
//创建表
dao.update(conn, "CREATE TABLE t_person (",
	"psn_id VARCHAR(32) NOT NULL PRIMARY KEY,",
	"psn_name VARCHAR(50) NOT NULL,",
	"psn_type VARCHAR(32) NOT NULL,",
	"psn_phone VARCHAR(20) NULL)");
```
更新的例子：
```java
//SQLMap对象方式，VALUES函数给出要更新的列名，列值的两两匹配的数组
SQLRunner dao = new SQLRunner(dataSource);
SQLMap smap = new SQLMap().UPDATE("t_person").VALUES(
		"psn_name", "张三",
		"psn_phone", "13112341234"
	).WHERE("psn_id", "=", id);
int num = dao.update(null, smap);

//SQL对象方式，其中SET、和V函数见SQL类的讲解
int num = dao.update(null, "UPDATE t_person",
	SET(
		"psn_name", "张三",
		"psn_phone", "13112341234"
	),
	"WHERE psn_id=", V(id)
);
```
插入的例子：
```java
//SQLMap对象方式
SQLRunner dao = new SQLRunner(dataSource);
SQLMap smap = new SQLMap().INSERT("t_person").VALUES(
	"psn_id", id,
	"psn_name", "张三",
	"psn_phone", "13112341234",
);
int num = dao.update(null, smap);

//SQL对象方式
int num = dao.update(null, "INSERT INTO t_person",
	FIELDS("psn_name", "psn_phone"),
	VALUES(
		"张三",
		"13112341234"
	),
	"WHERE psn_id=", V(id)
);
```

#### 2. SQL
该类提供了装配SQL语句的工具方法，其作用和SQLMap是一样的，即用于表达一条完整的SQL，不过SQL类的表现能力要强于SQLMap，类似于直接书写SQL语句。一条SQL语句可以分成任意多段，每一段要么是字符串，要么是一个SQL片段(List&lt;Object&gt;)，而SQL片段又可以用字符串或者颗粒度更小的SQL片段组合起来，如此往复。SQL类所做的工作就是将这些字符串和SQL片段拼接成SQL语句。
建议应用时以静态方式导入，这样使书写更简便易读，如下：
```java
import static com.thant.sqlgear.SQL.*;
// 静态导入之后，SQL所有的静态方法都可以省略SQL类名，比如SQL.V就可以写成V
```
主要常用方法：

| 方法名 | 说明 |
| ------------ | ------------ |
| V(Object value) | 表示值转换，将变量 **value** 的值嵌入SQL，同时生成占位符?，**value** 如果是数组将自动转变为(,,,,)的格式，可应用于IN查询。该函数返回一个SQL片段 |
| L(Object... args) | 表示连接，将变长参数**args**构成的多个SQL片段或字符串封装成一个SQL片段，以便嵌入其它工具方法。 |
| IF(boolean condition, Object... args)  | 如果条件 **condition** 的值为true，生成**args**代表的SQL片段 |
| IFElse(boolean condition, List truelst, List falselst) | 如果条件 **condition** 的值为true，生成 **truelist** 代表的SQL片段，否则生成生成 **falselist** 代表的SQL片段。该函数有多种参数形式，**truelist** 和 **falselist** 也可以是一个字符串。 |
| WHERE(Object... args) | 用于生成SQL语句的WHERE部分，参数是若干个SQL片段，每个SQL片段代表一个条件，条件之间用AND进行连接。 |
| WHERE_OR(Object... args) | 和WHERE函数不同的是，条件之间用OR进行连接。 |
| OR(Object... args) | 生成一个用OR逻辑连接的条件，参数必须都是用L或者IF等方法生成的SQL片段，每个参数即一个条件。 |
| AND(Object... args) | 生成一个用OR逻辑连接的条件，参数必须都是用L或者IF等方法生成的SQL片段，每个参数即一个条件。|
| FIELDS(Object... args) | 用于构成INSERT语句的更新字段列表，INSERT INTO后面的()部分 |
| VALUES(Object... args) | 用于构成INSERT和UPDATE语句的值列表，INSERT语句的VALUES子句，以及UPDATE语句SET子句中的值部分。 |
| SET(Object... args) | 用于构成UPDATE语句的SET子句的字段部分。 |
| fieldWithMap(Map<String, Object> map) | 用SQLMap来生成INSERT语句的更新字段列表 |
| setWithMap(Map<String, Object> map) | 用SQLMap来生成UPDATE语句的SET子句。 |
| valueWithMap(Map<String, Object> map) | 用SQLMap来生成INSERT和UPDATE语句的值列表。 |
| whereWithMap(Map<String, Object> map) | 用SQLMap来生成WHERE子句 |
| getSql | 返回值是一个包含占位符?的字符串。 |
| getArgs | 返回一个数组，和getSql返回字符串中的?占位符相对应。 |
**注：**SQLMap也有很多与之同名的函数，例如WHERE，注意区别使用。

#### 3. SpringSQL
为了减少依赖，SQLGear被设计成不依赖Spring框架，如果需要在Spring框架下运行，可以使用SpringSQL类，该类继承自SQLRuner，用法也与其一致。
在Spring环境中创建该类时，需要给出参数DataSource，该类会根据DataSource判断当前是否存在事务环境，如果存在事务，会继续使用该事务；如果没有事务，则从DataSource中创建新连接和事务。所以该类用完之后需要用Close函数来确保连接释放(存在Spring事务时不会释放，由Spring管理)。
```java
SpringSQL dao = new SpringSQL(dataSource); //这里会判断当前上下文是否存在Spring事务，有的话直接使用
...
dao.close(); //用完之后记得关闭
```

#### 4. SQLMap
该类继承自CommonMap，在其基础上增加了对SQL语句的各个组成部分进行了封装，在书写简单SQL语句时使用SQLMap是最佳的选择。常用函数包括：

| 键常量名 | 键对应值的含义  |
| ------------ | ------------ |
| SELECT  | 指定选择的表，可以是多表 |
| FROM | 指定查询的字段列表 |
| WHERE | WHERE表达式，以3个参数为一组构成一个查询条件，3个参数分别代表字段名、比较符号和比较值，每个查询条件之间用AND连接。 |
| WHERE_OR | 和WHERE一样，区别在于条件之间用OR连接。 |
| RDERBY | 指定排序方式。 |
| UPDATE | 指定要更新的表。 |
| VALUES | 要更新的字段和对应的值，以2个参数为一组。 |
| VALUESLIST | 指定要更新的全表各字段的值，需要和表字段一一对应。 |
| INSERT | 指定要插入的表。 |
| REPLACE | 指定要更新或插入的表，REPLACE会用WHERE条件检查插入的记录是否存在，从而选择是做UPDATE还是INSERT。 |
| query | 调用SQLRunner的query方法。 |
| update | 调用SQLRunner的update方法。 |

应用举例
```java
//SQLMap定义完之后直接使用SQLRunner执行并返回结果
SQLRunner dao = new SQLRunner(dataSource);
int rows = new SQLMap().INSERT("t_person").VALUES(
		"psn_id", "0",
		"psn_name", "xwf",
		"psn_type", "admin",
		"psn_phone", "12399990000"
	).update(dao); //注意这里直接调用SQLRunner了
System.out.println(rows+" rows inserted.");

//如果是对所有字段进行插入，可以不指定字段名，使用VALUELIST
rows = new SQLMap().INSERT("t_person")
	.VALUESLIST("1", "xwf", "admin", "12399990000").update(dao, conn);

//查询全表内容和表的总记录数
List<Map<String, Object>> lst =
	new SQLMap().FROM("t_person").query(dao);
long count = (long)new SQLMap()
	.SELECT("count(1)").FROM("t_person").query(dao);
	
//直接查询某个人的名字
String name = (String)new SQLMap().SELECT("psn_name")
	.FROM("t_person").WHERE("psn_id", "=", id)
	.query(dao);

//多表关联查询，等价于
//select * from t_person a, t_lesson b
//where a.psn_id = b.psn_id AND psn_phone = :phone;
List<Map<String, Object>> lst = new SQLMap()
	.FROM("t_person a, t_lesson b").WHERE(
		"a.psn_id", "=", "b.psn_id"
		,"a.psn_phone", "=", phone
	).query(dao);

//检查id=5的记录是否存在，存在就更新，不存在就插入
//注意：WHERE条件返回的记录数如果大于1会报错，以避免发生数据的错误覆盖
rows = new SQLMap().REPLACE("t_person").VALUES(
		"psn_name", "boss",
		"psn_type", "boss",
		"psn_phone", "12399990000"
	).WHERE("psn_id", "=", "5").update(dao, conn);
```
#### 5. CommonMap
该类主要的功能是用于简化多层级Map对象的构造，实现了Map &lt; String, Object &gt;接口。实现了Map接口的所有默认方法，并有两个新增函数：directGet和directPut，这两个方法可以用符号.来访问多层次嵌套的属性。.符号是默认的嵌套分隔符，可用putSpliter方法进行修改。
比如
```java
//如果中间存在空属性，直接返回null
map.directGet("fields.name.value");
```
表示获取map对象的属性名为fields的子map的属性名为name的孙子对象的value属性
如果用java自带的Map对象实现以上功能是下面这样的
```java
//不考虑可能存在空属性的情况，否则代码会更复杂
((Map<String, Object>)((Map<String, Object>)map.get("fields")).get("name")).get("value");
```
directPut函数也是类似的，不同的是，如果中间的map不存在会自动创建，下面的语句会创建一个3层map结构。map={ fields: { name: {value:100} } }
```java
CommonMap map = new CommonMap();
map.directPut("fields.name.value", 100);
```
也可以用CommonMap的构造函数直接实现
```java
//必须是奇数个参数，第一个参数为true表示将符号.处理为多层，为false将不进行分层处理
CommonMap map = new CommonMap(true,
		"fields.name.value", 100
);
```
CommonMap的构造有两种方式，一是默认构造，这时内部构造了一个LinkedhashMap对象，分有参和无参两种方式，有参数的例子如上面的例子。二是引用构造，这时需要传递一个Map对象作为构造参数，CommonMap内部引用该Map对象，对CommonMap所做的修改都是直接对源Map对象的修改。示例如下
```java
Map<String, Object> originMap = new HashMap<String, Object>();
CommonMap map = new CommonMap(originalMap);
map.directPut("fields.name", "thant");
originMap.get("fields"); //这里返回一个Map={name:"thant"}
```
如果不想引用源Map对象，可用copy方法进行拷贝。
```java
Map<String, Object> originMap = new HashMap<String, Object>();
CommonMap map = CommonMap.copy(originalMap);
```

更为详细的类参考请参考html文档

### 需求与支持
QQ 87421296(请备注sqlgear) EMAIL xwffwx@yeah.net
2020/03/02
