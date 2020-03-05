# SQLGear

### 介绍
sqlgear是用java语言实现的一个用于构建动态SQL并操作关系数据库的工具，基于[apache-dbutils](https://commons.apache.org/proper/commons-dbutils/)完成，并在其基础上进一步封装，内建了逻辑处理功能，使得直接在java代码中编写动态SQL更为简单方便，既避免了直接应用jdbc接口拼接字符串的问题，也不用像Mybatis等工具那样需要建立额外的xml和接口文件，提高了开发效率。

### 例子
一个简单的动态查询，其中**dataSource**是数据源，**table**是表名，**params**是[SQLMap类型]()(**sqlgear**封装的Map对象)的参数，**whereWithMap**函数会根据**params**中的字段和值动态构建查询条件，条件之间用AND连接
```java
SQLRunner dao = new SQLRunner(dataSource);
SQLMap cmap = new SQLMap(SQLMap.whereskey
	,"telephone", "=", phone
	,"psn_type", "<>", ptype);
List<Map<String, Object>> lst = dao.query(null, "select * from", table, whereWithMap(params));
```
等价于下面的SQL
```sql
select * from {table} where telephone = {phone} and psn_type <> {ptype}
```

在前面例子的基础上，增加逻辑处理能力（类似于Mybatis里的**&lt;IF&gt;**标签），变量**customOrder**指定排序字段名，如为null按默认字段psn_name排序，变量**pageNo**是页码，大于0表示需要分页，**pageSize**是每页的长度。函数**V**表示取值，会根据参数值的类型进行相应转换放入生成的SQL语句
```java
List<Map<String, Object>> lst = dao.query(null,
	"SELECT * FROM t_person", whereWithMap(params),
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
下面的例子演示了WHERE条件中AND和OR的混合使用，**WHERE**函数用于构造WHERE子句，默认是使用AND连接条件（也可以使用OR进行连接)，**OR**函数构造一个用OR进行连接的查询条件,**L**函数表示连接，用于构造**WHERE**和**OR**中的子条件
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
```java
SELECT * FROM t_person
WHERE psn_name like {name%} AND psn_type='personType_joiner'
	AND ( psn_org = {org} OR id IN {ids} )
```

### 组成结构
SQLGear的结构非常简单，由以下3部分组成
1. Map工具类：
CommonMap、SQLMap类用于SQL参数的封装。
2. SQL工具类：
SQL类，该类的作用是用于构造SQL语句，提供了对各种不同SQL语句片段的封装工具。
3. SQL执行类：
SQLRunner，SpringSQL类，SQLRunner类是对QueryRunner（请参考[apache-dbutils](https://commons.apache.org/proper/commons-dbutils/)）的封装。负责执行SQL语句。SpringSQL类继承了SQLRunner类，用于和Spring事物管理的集成。

### 类参考
1. CommonMap
实现了Map &lt; String, Object &gt;接口
其主要的功能是简化了对多层级Map对象的构造，其内部实现是基于LinkedHashMap。
该类的directGet和directPut方法可以用符号.（改分层符号可putSpliter方法进行修改）来访问多层次嵌套下的属性
比如
```java
//如果中间存在空属性，直接返回null
map.directGet("fields.name.value");
```
表示获取map对象的属性名为fields的子map的属性名为name的孙子对象的value属性
因此等价于
```java
//不考虑可能存在空属性的情况，否则更复杂
((Map<String, Object>)((Map<String, Object>)map.get("fields")).get("name")).get("value");
```
put函数也是类似的，不同的是，如果中间的map不存在会自动创建，下面的语句会创建一个3层map结构。
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
2. SQLMap
继承自CommonMap
3. SQL
4. SQLRunner
5. SpringSQL

### 需求与支持
QQ 87421296(请备注sqlgear) EMAIL xwffwx@yeah.net
2020/03/02

