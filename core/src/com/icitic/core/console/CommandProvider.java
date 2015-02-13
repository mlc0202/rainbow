package com.icitic.core.console;

import com.icitic.core.model.object.INameObject;

/**
 * 命令提供者扩展接口，如果一个Bundle提供console命令，应实现此接口并注册为扩展。
 * 
 * 对于一个名为 cmd 的命令，应实现一个 _cmd(ConsolePrinter printer) 函数来支持命令的执行
 * 
 * @author lijinghui
 * 
 */
public interface CommandProvider extends INameObject {

    public final static String EXTENSION_POINT = "core.CommandProvider";

    /**
     * 返回命令组描述
     * 
     * @return
     */
    public String getDescription();

    /**
     * @return 帮助信息
     */
    public void getHelp(StringBuilder sb);

}
