package utility;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectIO<T>
{
	public String rootDir;
	
	public ObjectIO(String rootDir)
	{
		this.rootDir = rootDir;
	}
	
	public void write(Object o, String filename)
	{
		File dir = new File(rootDir);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		
		try
		{
			FileOutputStream fout = new FileOutputStream(rootDir + "/" + filename);
			ObjectOutputStream out = new ObjectOutputStream(fout);
			
			out.writeObject(o);
			out.close();
			fout.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public T read(String filename)
	{	
		T o = null;
		
		try
		{
			FileInputStream fin = new FileInputStream(rootDir + "/" + filename);
			ObjectInputStream in = new ObjectInputStream(fin);
			
			o = (T) in.readObject();
			in.close();
			fin.close();
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return o;
	}
}
