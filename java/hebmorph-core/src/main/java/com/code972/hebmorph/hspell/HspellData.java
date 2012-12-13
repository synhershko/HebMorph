package com.code972.hebmorph.hspell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
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
	
	HspellData(String url) throws FileNotFoundException, IOException {
		if (url.charAt(url.length() - 1) != '/')
		{
			url += '/';
		}

		
		fdesc = getGzipStream(url, descFile);
		fstem = getGzipStream(url, stemsFile);
		fprefixes = getGzipStream(url, prefixesFile);
		fdict = getGzipStream(url, dictionaryFile);
		
		// Load the count of morphologic data slots required
		InputStream sizesStream = getResourceStream(url, sizesFile);
		try {
			String sizes = convertInputStreamToString(sizesStream);
			int index = sizes.indexOf(' ', sizes.indexOf('\n'));
			lookupLen = Integer.parseInt(sizes.substring(index+ 1).trim());
		}
		finally {
			sizesStream.close();
		}
	}

    private static InputStream getGzipStream(String baseUrl, String file) throws IOException {
        return new GZIPInputStream(getResourceStream(baseUrl, file));
    }

	private static InputStream getResourceStream(String baseUrl, String file) throws IOException {
        try {
            return new URL(baseUrl + file).openStream();
        } catch (MalformedURLException ex) {
            throw new IOException("Invalid URL supplied", ex);
        }
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
