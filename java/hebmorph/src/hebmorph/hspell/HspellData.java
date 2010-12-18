package hebmorph.hspell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;

public class HspellData {
	
	public static final String dictionaryFile = "hebrew.wgz";
	public static final String prefixesFile = dictionaryFile + ".prefixes";
	public static final String stemsFile = dictionaryFile + ".stems";
	public static final String descFile = dictionaryFile + ".desc";
	public static final String sizesFile = dictionaryFile + ".sizes";
	
	private final InputStream fdesc;
	private final InputStream fstem;
	private final InputStream fprefixes;
	private final InputStream fdict;
	private final int lookupLen;
	
	HspellData(String path) throws FileNotFoundException, IOException {		
		if (path.charAt(path.length() - 1) != File.separatorChar)
		{
			path += File.separatorChar;
		}

		
		fdesc = new GZIPInputStream(getResourceStream(path + descFile));
		fstem = new GZIPInputStream(getResourceStream(path + stemsFile));
		fprefixes = new GZIPInputStream(getResourceStream(path + prefixesFile));
		fdict = new GZIPInputStream(getResourceStream(path + dictionaryFile));
		
		// Load the count of morphologic data slots required
		InputStream sizesStream = getResourceStream(path + sizesFile);
		try {
			String sizes = convertInputStreamToString(sizesStream);
			int index = sizes.indexOf(' ', sizes.indexOf('\n'));
			lookupLen = Integer.parseInt(sizes.substring(index+ 1).trim());
		}
		finally {
			sizesStream.close();
		}
	}

	private static InputStream getResourceStream(String filepath) throws FileNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String classpath = filepath.replace(File.separatorChar, '/');
		InputStream stream = classLoader.getResourceAsStream(classpath);
		if (stream == null) {
			try {
				stream = new FileInputStream(filepath);
			}
			catch (FileNotFoundException e) {
				throw new FileNotFoundException("Cannot find " + classpath + " in classpath nor in " + new File(filepath).getAbsolutePath()); 
			}
		}
		return stream;
	}
	
	void close() throws IOException {
		fdesc.close();
		fstem.close();
		fprefixes.close();
		fdict.close();
	}

	public InputStream getDescriptionStream() {
		return fdesc;
	}

	public InputStream getStemStream() {
		return fstem;
	}

	public InputStream getPrefixesStream() {
		return fprefixes;
	}

	public InputStream getDictionaryStream() {
		return fdict;
	}

	public int getLookupLength() {
		return lookupLen;
	}
	
	private static String convertInputStreamToString(InputStream stream) throws IOException
	{
		InputStreamReader input = new InputStreamReader(stream, "UTF-8");
		StringWriter output = new StringWriter();
        char[] buffer = new char[1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return output.toString();
	}
}
