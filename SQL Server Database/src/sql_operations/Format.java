package sql_operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Process a text file for large insertion within a SQL Server database.
 * 
 * @author Isidore Sossa
 */
public class Format
{
	/**
	 * 
	 */
	private static String inputFileExtension;
	
	/**
	 * 
	 */
	private static PrintWriter printer;
	
	private static String regExStr;
	
	/**
	 * @param message
	 * @return
	 */
	private static String EOPMessage(String message) // EOP: END OF OPERATION
	{
		return message;
	}
	
	/**
	 * @param filenameIn
	 * @return
	 * @throws IOException
	 */
	public static String formatRecordForInsertion(String filenameIn) throws IOException
	{
		String fileExtension = filenameIn.substring(filenameIn.indexOf(".") + 1); 
		Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
		
		if (Format.inputFileExtension == null)
		{
			System.out.printf("Enter input file extension (csv, txt, etc.): ");
			setInputFileExtension(in.nextLine());
		}
		else
		{
			Format.inputFileExtension = "csv";
		}
		
		if (fileExtension.equals(Format.inputFileExtension))
		{
			if (Files.exists(Paths.get(filenameIn)))
			{
				String filenameOut = getFilenameOut(filenameIn);
				printer = new PrintWriter(filenameOut);
				Stream<String> inStream = Files.lines(Paths.get(filenameIn));
				
				// Regex to be matched
				if (Format.regExStr == null)
				{
					System.out.println("Enter regular expression to delimit fields of each record:");
					setRegularExpression(in.nextLine());
				}
				
				System.out.printf("Enter number of fields per record: ");
				int numberOfFields = in.nextInt();
				
				// Step 1: Compile a regex via static method Pattern.compile(), default is case-sensitive
				Pattern pattern = Pattern.compile(Format.regExStr);
				// Pattern.compile(regex, Pattern.CASE_INSENSITIVE);  // for case-insensitive matching
				
				inStream.forEach(record -> {
					// Step 2: Allocate a matching engine from the compiled regex pattern,
					//         and bind to the input string
					Matcher matcher = pattern.matcher(record);
					
					String[] fields = new String[numberOfFields];
					
					if (matcher.find())
					{
						for (int index = 1; index <= numberOfFields; index++)
						{
							String field = matcher.group(index * 2);
							
							if (field == null)
							{
								fields[index - 1] = String.format("'%s'", "");
							}
							else
							{
								if (field.contains("\'"))
								{
									field = field.replace("\'", "''");
								}
								
								fields[index - 1] = String.format("'%s'", field);
							}
						}
					}
					
					String delimiter = ",";
					
					Stream<String> recordStream = Arrays.stream(fields);
					
					Optional<String> out = recordStream.reduce((field1, field2) -> field1 + delimiter + field2);

					printer.println(String.format("(%s),", out.get()));
				});
				
				printer.close();
				inStream.close();
				in.close();
				
				return EOPMessage(String.format("File saved to '%s'.", filenameOut));
			}
			else
			{
				in.close();
				
				throw new IOException(String.format("File '%s' could not be opened."
						+ " Check file name and try again.", filenameIn));
			}
		}
		else
		{
			in.close();
			
			throw new IOException(String.format("Expected '%s' but received '%s'."
					+ " Check file type and try again.", fileExtension, Format.inputFileExtension));
		}
	}
	
	/**
	 * @param filenameIn
	 * @return
	 */
	private static String getFilenameOut(String filenameIn)
	{
		return filenameIn.substring(0, filenameIn.indexOf('.')) + "_out.txt";
	}
	
	/**
	 * @param fileExtension
	 */
	public static void setInputFileExtension(String fileExtension)
	{
		inputFileExtension = fileExtension;
	}
	
	/**
	 * @param regExStrIn
	 */
	public static void setRegularExpression(String regExStrIn)
	{
		Format.regExStr = regExStrIn;
	}
}
