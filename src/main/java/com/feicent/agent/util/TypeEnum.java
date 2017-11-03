package com.feicent.agent.util;

public enum TypeEnum {
	// 枚举成员名称全部大写
	DEFAULT("默认",0), CMD("执行命令",1), SHELL("执行命令返回数据", 2), UPLOAD("上传",3), DOWNLOAD("下载",4);  

	// 成员变量  
    private String name;
    private int type;
    
    // 构造方法 ,强制私有的
    private TypeEnum(String name, int type) {  
        this.name = name;  
        this.type = type;  
    }
    
    // 普通方法  
    public static String getName(int type) {  
    	for (TypeEnum c : TypeEnum.values()) {  
            if (c.getType() == type) {  
                return c.name;  
            }  
        }  
        return null;  
    } 
    
    public static TypeEnum from(int type) {
    	for (TypeEnum c : TypeEnum.values()) {
    		if (c.getType() == type) {  
                return c;  
            } 
		}
		return null;
	}

    public String getName() {  
        return name;  
    }  
    public void setName(String name) {  
        this.name = name;  
    }  
    public int getType() {  
        return type;  
    }  
    public void setType(int type) {  
        this.type = type;  
    }  
    
    @Override  
    public String toString() {  
        return this.name;  
    }
}
