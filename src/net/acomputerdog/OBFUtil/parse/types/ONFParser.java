package net.acomputerdog.OBFUtil.parse.types;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import com.google.common.collect.Lists;

import net.acomputerdog.OBFUtil.map.TargetType;
import net.acomputerdog.OBFUtil.parse.FileParser;
import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.parse.URLParser;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.table.OBFTableSRG;
import net.acomputerdog.OBFUtil.util.Obfuscator;

/**
 * 
 * Parser for reading and writing a dense obfuscation mappings file (.onf).
 *  <p>
 *  <li>AccessTransformer directives may also be included but are not parsed.</li>
 *  <li>Other files may be side loaded using >>{file} notation.</li>
 *  <li>All srg names are given without their type prefix (field_/func_). It will be inferred from the context.</li>
 *  </p>
 *  <p>
 *  <b>Format:</b>
 *  <pre>{obfuscated package}:{mcp package}
 *  	{obfuscated class}:{mcp class}
 *  		{obfuscated field}:{srg field}:{mcp field}
 *  		<init> {mcp descriptor}
 *  		{obfuscated method name}:{srg method name}:{mcp method name} {mcp descriptor}
 *  # Comments may be included as such</pre>
 *  <b>AT (enhanced) format:</b>
 *  <pre>{obfuscated package}:{mcp package}
 *  	{ac}{f}{m}!{obfuscated class}:{mcp class}
 *  		{ac}!{obfuscated field}:{srg field}:{mcp field}
 *  		{ac}!<init> {mcp descriptor}
 *  		{ac}!{obfuscated method name}:{srg method name}:{mcp method name} {mcp descriptor}</pre>
 *  </p>
 *  <p>
 *  ac: Access Transformation
 *  <br> Possible values:
 *  <li>public</li>
 *  <li>protected</li>
 *  <li>private</li>
 *  <li>package</li>
 *  <li>-f (remove final)</li>
 *  <li>+f (add final) (not recommended)</li>
 *  </p>
 *  <p>
 *  ac's applied to a class are applied to all members inside that class. 
 *  <li>'m' marker says it must be applied to methods</li>
 *  <li>'f' marker says it must be applied to fields.</li>
 *  <br>Both may be combined, but one must be present at all times.
 *  </p>
 */
public class ONFParser extends FileParser implements URLParser {
	private final Obfuscator obfuscator = new Obfuscator();
	
	private String activeDirectory = null;
	
	private final List<DetectedTransformation> transformations = Lists.newArrayList();
	private final List<String> seenFiles = Lists.newArrayList();
	
	/**
	 * Gets the list of access transformations found whilst parsing onf entries.
	 */
	public List<DetectedTransformation> getDetectedTransformations() {
		return transformations;
	}
	
	@Override
	public void loadEntries(File file, OBFTable table, boolean overwrite) throws IOException {
		activeDirectory = file.getParent();
		seenFiles.add(file.getName());
		loadEntries(new FileInputStream(file), table, overwrite);
		activeDirectory = null;
		seenFiles.clear();
	}
	
	@Override
	public void loadEntries(URL url, OBFTable table, boolean overwrite) throws IOException {
		String path = URLDecoder.decode(url.getPath().replace("\\", "/"),"UTF-8");
		activeDirectory = path.substring(0, path.lastIndexOf("/"));
		seenFiles.add(path.replace(activeDirectory, ""));
		loadEntries(url.openStream(), table, overwrite);
		activeDirectory = null;
		seenFiles.clear();
	}
	
	private void loadEntries(InputStream stream, OBFTable table, boolean overwrite) throws IOException {
        if (stream == null) throw new IllegalArgumentException("InputStream cannot be null!");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(stream));
            parseFile(in, table, overwrite);
        } catch (IOException e) {
        	throw new IOException("Exception whilst reading file", e);
        } catch (IllegalArgumentException e) {
        	throw new IOException("Exception whilst reading file", e);
        } finally {
            if (in != null) in.close();
        }
    }
	
	/**
	 * Side loads an imported file as if it were a part of this one.
	 * <p>
	 * Referenced files are always taken relative to the current directory.
	 * 
	 * @param fileName	Name of file referenced
	 * @param table		Table to read into
	 * @param overwrite	Tru to replace existing values in the table
	 */
	private void handleImport(String fileName, OBFTable table, boolean overwrite) {
    	if (seenFiles.contains(fileName)) return;
    	InputStream input = null;
    	try {
    		if (activeDirectory.contains("!")) {
        		input = ONFParser.class.getResourceAsStream(activeDirectory.split("!")[1].concat("/").concat(fileName));
        	} else {
        		File f = new File(activeDirectory, fileName);
        		if (f.exists()) input = new FileInputStream(f);
        	}
	    	if (input != null) {
	    		seenFiles.add(fileName);
				loadEntries(input, table, overwrite);
	    	} else {
	    		throw new IllegalArgumentException("File \"" + activeDirectory.concat("/").concat(fileName) + "\" could not be opened.");
	    	}
    	} catch (IOException e) {
			(new IllegalArgumentException("Exception whilst sideloading file: \"" + fileName + "\". Skipping.", e)).printStackTrace();
		}
    }
	
	@Override
	public void storeEntries(File file, OBFTable table) throws IOException {
		storeEntries(new FileOutputStream(file), table);
	}
	
	public void storeEntries(OutputStream stream, OBFTable table) throws IOException {
		if (stream == null) throw new IllegalArgumentException("OutputStream must not be null!");
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(stream));
            if (table instanceof OBFTableSRG) {
                writeTableSRG(out, (OBFTableSRG) table);
            } else {
                writeTableNormal(out, table);
            }
        } finally {
            if (out != null) out.close();
        }
	}
	
	@Override
    protected void parseFile(BufferedReader in, OBFTable table, boolean overwrite) throws IOException {
    	boolean takeSrg = table instanceof OBFTableSRG;
    	String[] activePackage = null;
    	String[] activeClass = null;
    	List<Object[]> retroActive = Lists.newArrayList();
    	String i;
        while ((i = in.readLine()) != null) {
        	if (i.isEmpty() || i.startsWith("#")) continue;
        	i = i.split("#")[0];
        	if (i.startsWith(">>")) {
        		handleImport(i.substring(2, i.length()), table, overwrite);
        		continue;
        	}
        	TargetType type = typeof(i);
        	i = i.trim();
        	String[] parsed = null;
        	switch (type) {
        		case PACKAGE:
	        		activePackage = parsed = unambiguate(i, type);
	        		break;
        		case CLASS:
        			activeClass = parsed = unambiguate(i, type);
        			if (activePackage == null) throw new FormatException("Class before package at \n" + i);
        			parsed = prependClassAndPackage(parsed, activePackage);
        			break;
        		case FIELD:
        			if (activeClass == null) throw new FormatException("Field before class at \n" + i);
        			parsed = prependClassAndPackage(unambiguate(i, type), activeClass);
        			break;
        		case CONSTRUCTOR:
        		case METHOD:
        			if (activeClass == null) throw new FormatException("Method/Constructor before class at \n" + i);
        			if (i.indexOf(" ") == -1) throw new FormatException("Missing Method/Constructor arguments at \n" + i);
        			parsed = prependClassAndPackage(unambiguate(i, type), activeClass);
        			retroActive.add(new Object[] { type, parsed, i.indexOf("\\!") != -1 ? null : i.split("\\!")[0] });
        			continue;
        	}
        	if (i.indexOf("!") != -1 && type == TargetType.CLASS || type == TargetType.FIELD) {
    			transformations.add(new DetectedTransformation(i.split("!")[0], parsed[2], type));
    		}
        	if (overwrite | !table.hasObf(parsed[0], type)) {
	        	if (takeSrg) {
	        		((OBFTableSRG)table).addTypeSRG(parsed[0], parsed[1], parsed[2], type);
	        	} else {
	        		table.addType(parsed[0], parsed[2], type);
	        	}
        	}
        }
        for (Object[] j : retroActive) {
        	String[] parsed = (String[])j[1];
        	String deobfuscatedDecriptor = parsed[2].split(" ")[1];
        	String obfuscatedDescriptor = obfuscator.obfuscateDescriptor(deobfuscatedDecriptor, table);
        	TargetType type = (TargetType)j[0];
        	parsed[0] = parsed[0].split(" ")[0] + " " + obfuscatedDescriptor;
        	parsed[1] = parsed[1].split(" ")[0] + " " + deobfuscatedDecriptor;
        	if (overwrite | !table.hasObf(parsed[0], type)) {
	        	if (takeSrg) {
	        		((OBFTableSRG)table).addTypeSRG(parsed[0], parsed[1], parsed[2], type);
	        	} else {
	        		table.addType(parsed[0], parsed[2], type);
	        	}
        	}
        	if (j[2] != null) {
        		transformations.add(new DetectedTransformation((String)j[2], parsed[2], type));
        	}
        }
    }
    
    protected void writeTableNormal(Writer out, OBFTable table) throws IOException {
    	String[] packages = table.getAllDeobf(TargetType.PACKAGE);
        String[] classes = table.getAllDeobf(TargetType.CLASS);
        String[] fields = table.getAllDeobf(TargetType.FIELD);
        String[] methods = table.getAllDeobf(TargetType.METHOD);
        String[] constructors = table.getAllDeobf(TargetType.CONSTRUCTOR);
        for (String pack : packages) {
        	String obf = table.obf(pack, TargetType.PACKAGE);
        	out.write(obf + ":" + pack + "\n");
        	for (String clazz : classes) {
        		if (clazz.indexOf(pack) == 0) {
        			String clazzName = clazz.replace(pack + ".", "");
        			String clazzNameObf = table.obf(clazz, TargetType.CLASS).replace(obf, "");
        			out.write("\t" + clazzNameObf + ":" + clazzName);
        			for (String field : fields) {
        				if (field.indexOf(clazz) == 0) {
        					String fieldName = field.replace(clazz + ".", "");
        					String fieldNameObf = table.obf(field, TargetType.FIELD).replace(clazz + ".", "");
        					out.write("\t\t" + fieldNameObf + ":" + fieldName);
        				}
        			}
        			for (String constr : constructors) {
        				if (constr.indexOf(clazz) == 0) {
        					out.write("\t\t<init> " + constr.split(" ")[1] + "\n");
        				}
        			}
        			for (String method : methods) {
        				if (method.indexOf(clazz) == 0) {
        					String desc = method.split(" ")[1];
        					String methodName = method.replace(clazz + ".", "").split(" ")[0];
        					String methodNameObf = table.obf(method, TargetType.METHOD).replace(clazz + ".", "").split(" ")[0];
        					out.write("\t\t" + methodNameObf + ":" + methodName + " " + desc);
        				}
        			}
        		}
        	}
        }
    }
    
    protected void writeTableSRG(Writer out, OBFTableSRG table) throws IOException {
    	String[] packages = table.getAllDeobf(TargetType.PACKAGE);
        String[] classes = table.getAllDeobf(TargetType.CLASS);
        String[] fields = table.getAllDeobf(TargetType.FIELD);
        String[] methods = table.getAllDeobf(TargetType.METHOD);
        String[] constructors = table.getAllDeobf(TargetType.CONSTRUCTOR);
        for (String pack : packages) {
        	String obf = table.obf(pack, TargetType.PACKAGE);
        	out.write(obf + ":" + pack + "\n");
        	for (String clazz : classes) {
        		if (clazz.indexOf(pack) == 0) {
        			String clazzName = clazz.replace(pack + ".", "");
        			String clazzNameObf = table.obf(clazz, TargetType.CLASS).replace(obf, "");
        			out.write("\t" + clazzNameObf + ":" + clazzName + "\n");
        			for (String field : fields) {
        				if (field.indexOf(clazz) == 0) {
        					String fieldName = field.replace(clazz + ".", "");
        					String fieldNameSrg = table.getSRGFromDeObf(field, TargetType.FIELD).replace(clazz + ".", "");
        					String fieldNameObf = table.obf(field, TargetType.FIELD).replace(clazz + ".", "");
        					out.write("\t\t" + fieldNameObf + ":" + fieldNameSrg + ":" + fieldName + "\n");
        				}
        			}
        			for (String constr : constructors) {
        				if (constr.indexOf(clazz) == 0) {
        					out.write("\t\t<init> " + constr.split(" ")[1] + "\n");
        				}
        			}
        			for (String method : methods) {
        				if (method.indexOf(clazz) == 0) {
        					String desc = method.split(" ")[1];
        					String methodName = method.replace(clazz + ".", "").split(" ")[0];
        					String methodNameSrg = table.getSRGFromDeObf(method, TargetType.METHOD).replace(clazz + ".", "").split(" ")[0];
        					String methodNameObf = table.obf(method, TargetType.METHOD).replace(clazz + ".", "").split(" ")[0];
        					out.write("\t\t" + methodNameObf + ":" + methodNameSrg + ":" + methodName + " " + desc + "\n");
        				}
        			}
        		}
        	}
        }
    }
    
    private String[] prependClassAndPackage(String[] arr, String[]... components) {
    	for (int i = 0; i < arr.length; i++) {
    		for (int j = components.length - 1; j >= 0; j--) {
    			if (!components[j][i].isEmpty()) {
    				arr[i] = components[j][i] + "." + arr[i];
    			}
    		}
    	}
    	return arr;
    }
    
    /**
     * Converts a line to a triplet of notch/srg/mcp values.
     * <p>
     * The first and last are always assumed.
     * If there are only two values {srg} will take on the same value as {mcp}.
     * If there is only one value they are all taken as the same.
     * 
     * @param line	Line to parse
     * @param type	Type Context
     * @return Three part string with each extracted value
     */
    private String[] unambiguate(String line, TargetType type) {
    	if (line.indexOf('!') != -1) {
    		line = line.split("!")[1];
    	}
    	if (line.indexOf(':') != -1) {
    		String[] split = line.split(":");
    		if (split.length >= 3) {
    			if (type == TargetType.METHOD) {
    				split[1] = "func_" + split[1];
    			} else if (type == TargetType.FIELD) {
    				split[1] = "field_" + split[1];
    			}
    			return new String[] { split[0], split[1], split[2] };
    		}
    		return new String[] { split[0], split[1], split[1] };
    	}
    	return new String[] { line, line, line };
    }
    
    private TargetType typeof(String raw) {
    	if (raw.startsWith("\t\t")) {
    		if (raw.trim().indexOf(' ') == -1) return TargetType.FIELD;
    		if (raw.indexOf("<iinit>") == 0) return TargetType.CONSTRUCTOR;
    		return TargetType.METHOD;
    	}
    	if (raw.startsWith("\t")) return TargetType.CLASS;
    	return TargetType.PACKAGE;
    }
    
    public class DetectedTransformation {
    	public final boolean isGlobal;
    	
    	public final String directives;
    	public final String mcpTarget;
    	public final TargetType targetType;
    	
    	private DetectedTransformation(String transform, String target, TargetType type) throws FormatException {
    		mcpTarget = target;
    		isGlobal = type == TargetType.CLASS;
    		if (isGlobal) {
    			boolean hasM = transform.lastIndexOf('m') >= transform.length() - 2;
    			boolean hasF = transform.lastIndexOf('f') >= transform.length() - 2;
    			if (hasM || hasF) {
    				transform = transform.substring(0, transform.length() - 1);
    				if (hasM) {
    					targetType = TargetType.METHOD;
	    				if (hasF) {
	    					transform = transform.substring(0, transform.length() - 1);
	    					transformations.add(new DetectedTransformation(transform, target, TargetType.FIELD, true));
	    				}
    				} else {
    					targetType = TargetType.FIELD;
    				}
    			} else {
    				throw new FormatException("Global access transformations must have a target type flag (f|m) at \n" + transform + "!" + target);
    			}
    		} else {
    			targetType = type;
    		}
    		directives = transform;
    	}
    	
    	private DetectedTransformation(String transform, String target, TargetType type, boolean global) {
    		directives = transform;
    		mcpTarget = target;
    		targetType = type;
			isGlobal = global;
    	}
    }
}