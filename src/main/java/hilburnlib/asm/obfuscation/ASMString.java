package hilburnlib.asm.obfuscation;

import net.minecraftforge.classloading.FMLForgePlugin;

public class ASMString
{
    public static boolean OBFUSCATED = FMLForgePlugin.RUNTIME_DEOBF;
    private String text;

    public ASMString(String text)
    {
        this.text = text;
    }

    public ASMString(Class clazz)
    {
        this.text = clazz.getCanonicalName();
    }

    public String getText()
    {
        return text;
    }

    public String getReadableText()
    {
        return text;
    }

    public String getASMClassName()
    {
        return text.replaceAll("\\.","/");
    }

    public String getASMTypeName()
    {
        return "L" + getASMClassName() +";";
    }

    public static class ASMObfString extends ASMString
    {
        private String obfText;
        public ASMObfString(String text, String obfText)
        {
            super(text);
            this.obfText = obfText;
        }

        @Override
        public String getASMClassName()
        {
            return OBFUSCATED ? obfText : super.getASMClassName();
        }

        @Override
        public String getText()
        {
            return OBFUSCATED ? obfText : super.getText();
        }
    }
}
