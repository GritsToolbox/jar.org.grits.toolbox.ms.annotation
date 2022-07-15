package org.grits.toolbox.ms.annotation.structure;

public class StructureHandlerException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public StructureHandlerException()
    {
        super();
    }
    public StructureHandlerException(String a_message)
    {
        super(a_message);
    }
    public StructureHandlerException(String a_message, Throwable a_exception)
    {
        super(a_message,a_exception);
    }
}
