package org.zachary.tools.encodingConversion;

import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zachary.modules.io.FileCopyUtils;
import org.zachary.modules.utils.Commons;
import org.zachary.modules.utils.Strings;

/**
 * 将某个目录下的文件转为指定的编码方式的文件，转换后写入同一个文件。如果提供了源文件的编码方式，则按此编码方式读取文件，否则采
 * 取自动探测方式得到源文件的编码方式。
 * 
 * @author Zachary Guo
 * @since 2013-10-3 上午3:10:38
 */
public class EncodingConversion {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(EncodingConversion.class);

	public static void main(String[] args) throws Exception {
		
		Long startTime = System.currentTimeMillis();
		
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
		CmdLineParser.Option pathOpt = parser.addStringOption('p', "path");
		CmdLineParser.Option fileEncodingOpt = parser.addStringOption('f', "file-encoding");
		CmdLineParser.Option convertToOpt = parser.addStringOption('t', "convert-to");
		
		parser.parse(args);
		
		Boolean help = (Boolean) parser.getOptionValue(helpOpt);
		if (help != null && help.booleanValue()) {
			usage();
			System.exit(0);
		}
		
		String path = (String) parser.getOptionValue(pathOpt);
		if (path == null) {
			logger.error("-p, --path, 待转换编码的文件所在的目录必须提供");
			printSystemInfo(startTime, false);
			System.exit(0);
		}
		// 不管传入 path 以 / 还是 \ 作为文件分隔符，都将其替换为操作系统的文件分隔符，同时将连续出现多个文件分隔符改为一个
		path = path.replaceAll("\\/|\\\\", "\\" + File.separator).replaceAll("(\\" + File.separator + ")+","\\" + File.separator);
		// 如果 path 以 File.separator 结尾，去掉最后的这个 File.separator
		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() - File.separator.length());
		}
		
		logger.info("Preparation OK");
		logger.info("");
		logger.info("------------------------------------------------------------------------");
		logger.info("Scanning for " + path + " ...");
		logger.info("------------------------------------------------------------------------");
		
		String fileEncoding = (String) parser.getOptionValue(fileEncodingOpt);
		String convertToEncoding = (String) parser.getOptionValue(convertToOpt);
		if (Strings.isBlank(convertToEncoding)) {
			logger.error("-t, --convert-to, 转换后的编码方式必须提供");
			printSystemInfo(startTime, false);
			System.exit(0);
		}
		if (convertToEncoding.equalsIgnoreCase(fileEncoding)) {
			// 转换前和转换后的编码方式一样，不做任何处理，直接退出
			printSystemInfo(startTime, true);
			System.exit(0);
		}
		
		try {
			scan(new File(path), fileEncoding, convertToEncoding);
		} catch (IllegalStateException e) {
			logger.warn(e.getMessage());
			printSystemInfo(startTime, false);
			System.exit(0);
		}
		
		printSystemInfo(startTime, true);
	}
	
	private static void usage() {
		System.out.println("\nUsage: java -jar encodingConversion-x.y.z.jar [options]\n\nGlobal Options\n  " +
				"-h, --help                                   Displays this information\n  " +
				"-p, --path                                   待转换编码的文件所在的目录\n  " +
				"-f, --file-encoding                          待转换编码的文件的原始编码，若未提供，则采取自动探测原始编码\n  " +
				"-t, --convert-to                             待转换编码的文件的要转换成的编码\n  ");
	}
	
	/**
	 * 输出信息运行信息
	 * 
	 * @param startTime 起始执行时间
	 * @param isSucess 执行是否成功。true for suecess, otherwose false
	 */
	private static void printSystemInfo(long startTime, boolean isSucess) {
		logger.info("------------------------------------------------------------------------");
		logger.info("BUILD " + (isSucess ? "SUCESS" : "FAILED"));
		logger.info("------------------------------------------------------------------------");
		logger.info("Total time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "s");
		logger.info("Finished at: " + new Date());
		logger.info("------------------------------------------------------------------------");
		if (! isSucess) {
			logger.error("Failed to execute encodingConversion");
			logger.error("");
			logger.error("Re-run encodingConversion using --help option:");
			logger.error("    java -jar encodingConversion-x.y.z.jar --help");
		}
	}
	
	/**
	 * 递归扫描 file，如果是文件，则实施编码转换操作。
	 * 
	 * @param file 待扫描的文件或目录
	 * @param encoding 源文件的编码方式
	 * @param toEncoding 待转换成的编码方式
	 */
	private static void scan(File file, String encoding, String toEncoding) {
		if (file.isDirectory()) {
			// file 是目录，遍历其下的文件继续 scan
			for (File subFile : file.listFiles()) {
				scan(subFile, encoding, toEncoding);
			}
		} else {
			doConversion(file, encoding, toEncoding);
		}
	}
	
	/**
	 * 执行文件编码转换
	 * 
	 * @param file 待转换的文件
	 * @param encoding 该文件的原始编码方式
	 * @param toEncoding 转换成的编码方式
	 */
	private static void doConversion(File file, String encoding, String toEncoding) {
		assert file.isFile();
		logger.info("Processing file " + file.getAbsolutePath() + " ...");

		try {
			// 源文件对应的字节数组对象
			byte[] bytes = FileCopyUtils.copyToByteArray(file);
			
			// 探测到的文件编码方式
			String detectedEncoding = Commons.getCharset(bytes);
			if (Strings.isBlank(encoding)) {
				// 未提供源文件的编码方式，采取自动探测到的文件编码
				encoding = detectedEncoding;
			} else {
				// 用户提供的文件编码方式，和探测到的文件编码方式进行比较。如果不相同，强制退出，以免转换后出现乱码
				if (! encoding.equalsIgnoreCase(detectedEncoding)) {
					throw new IllegalStateException("用户提供的文件编码方式为 " + encoding + ", 自动探测到文件编码方式为 " +
						detectedEncoding + "。为避免转换后出现乱码，强制退出。");
				}
			}
			
			if (toEncoding.equalsIgnoreCase(encoding)) {
				return;
			}
			
			// 以 encoding 方式读取文件
			String content = new String(bytes, encoding);
			// 把文件转为 toEncoding
			bytes = content.getBytes(toEncoding);
			// 将转换后的字节数组写入同一个文件
			File out = new File(file.getAbsolutePath());
			FileCopyUtils.copy(bytes, out);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
