package com.github.digdeep126.mycatutil;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

/**
 * 利用mybatis拦截器实现 @readonly 的sql路由到slave的功能
 * @author digdeep@126.com
 */
@Intercepts({@Signature(type=StatementHandler.class, method="prepare", args={Connection.class})})  
public class ReadOnlyInterceptor implements Interceptor{
        
    public Object intercept(Invocation invocation) throws Throwable {  
        if(invocation.getTarget() instanceof RoutingStatementHandler){    
            RoutingStatementHandler statementHandler = (RoutingStatementHandler)invocation.getTarget();    
            StatementHandler delegate = (StatementHandler) ReflectHelper.getFieldValue(statementHandler, "delegate");    
            BoundSql boundSql = delegate.getBoundSql();  
            
            //拦截到的prepare方法参数是一个Connection对象    
            Connection connection = (Connection)invocation.getArgs()[0];    
            if(connection.isReadOnly()){
            	// 旧版： 设置自动提交，可以负载均衡到 slave 上
            	connection.setAutoCommit(true);
            	
            	// 新版1.5,2.0可以使用新注解：/*#mycat:db_type=slave*/，保证一定在slave上运行
            	
            	 //获取当前要执行的Sql语句，也就是我们直接在Mapper映射语句中写的Sql语句    
                String sql = boundSql.getSql();    
                sql = new StringBuilder("/*#mycat:db_type=slave*/").append(sql).toString();
                
                //利用反射设置当前BoundSql对应的sql属性为我们建立好的分页Sql语句    
                ReflectHelper.setFieldValue(boundSql, "sql", sql);  
            }
           
        }    
        return invocation.proceed();    
    }  

    /**  
     * 拦截器对应的封装原始对象的方法  
     */          
    public Object plugin(Object arg0) {    
        // TODO Auto-generated method stub    
        if (arg0 instanceof StatementHandler) {    
            return Plugin.wrap(arg0, this);    
        } else {    
            return arg0;    
        }   
    }

	@Override
	public void setProperties(Properties properties) {
		
	}    

}
