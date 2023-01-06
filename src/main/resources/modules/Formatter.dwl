%dw 2.0
fun formatAny(inputData: Any) = if (inputData.^mimeType == "application/xml" or
									   inputData.^mimeType == "application/dw" or
									   inputData.^mimeType == "application/json") 
									write(inputData,inputData.^mimeType,{indent:false}) 
								   else if (inputData.^mimeType == "*/*")
								    inputData
								   else
						   	write(inputData,inputData.^mimeType)
						   	
fun formatNonJSON(inputData: Any) = if (inputData.^mimeType == "application/xml" or
										   inputData.^mimeType == "application/dw") 
										 write(inputData,inputData.^mimeType,{indent:false}) 
									   else if (inputData.^mimeType == "application/json" or inputData.^mimeType == "*/*")
									   	 inputData
									   else
							   			 write(inputData,inputData.^mimeType)

fun formatAnyWithMetadata(inputData: Any) = {
												 data: if (inputData.^mimeType == "application/xml" or
														   inputData.^mimeType == "application/dw" or
														   inputData.^mimeType == "application/json")
														 write(inputData,inputData.^mimeType,{indent:false})
                                                       else if (inputData.^mimeType == "*/*")
                                                        inputData
													   else
													     write(inputData,inputData.^mimeType),													
												 (contentLength: inputData.^contentLength) if (inputData.^contentLength != null),
												 (dataType: inputData.^mimeType) if (inputData.^mimeType != null),
												 (class: inputData.^class) if (inputData.^class != null)
											   } 

fun formatNonJSONWithMetadata(inputData: Any) = {
												 data: if (inputData.^mimeType == "application/xml" or
														   inputData.^mimeType == "application/dw")
														 write(inputData,inputData.^mimeType,{indent:false})
													   else if (inputData.^mimeType == "application/json" or inputData.^mimeType == "*/*")
													   	 inputData
													   else
													     write(inputData,inputData.^mimeType),													
												 (contentLength: inputData.^contentLength) if (inputData.^contentLength != null),
												 (dataType: inputData.^mimeType) if (inputData.^mimeType != null),
												 (class: inputData.^class) if (inputData.^class != null)
											   } 
