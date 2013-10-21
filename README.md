### 概述
encodingConversion 是用 java 开发的，用于将某个文件夹下的所有文件转为指定编码的文件。你可以告知 encodingConversion 源文件的编码，这个编码之针对整个目录而言，即这个目录下的所有文件的源编码是什么。也可以不告知这个信息，encodingConversion 会自动探测每个源文件的编码方式。

### 使用方法
```cmd
java -jar encodingConversion-x.y.z.jar --path ${path} --file-encoding ${fileEncoding} --convert-to ${convertTo}
```
简要说明下，在 cmd 窗口，执行以上命令即可运行 encodingConversion。其中，encodingConversion-x.y.z.jar 中的 x.y.z 是其版本号。目前发布的版本是 1.0，即 markdownHelper-1.0.jar。`--path` 参数代表的就是你的要转换编码的文件所在目录。`--file-encoding` 参数用于指示源文件的原始编码方式，此编码是针对整个目录而言的。`--convert-to` 参数用于指示要转换的目标编码方式。

假设你的要转换编码的文件所在目录位于 `D:/My Folder`，目前使用的 encodingConversion 的版本是 1.0，那么你可以使用以下命令来运行 markdownHelper：
```cmd
java -jar encodingConversion-1.0.jar --path "D:/My Folder" --convert-to utf-8
```
通常情况下，***你不需要指定源文件的编码方式***，即不需要提供 `--file-encoding` 参数。因为不管在何种情况下，encodingConversion 都会执行源文件的原始编码的探测，如果探测出来的和你提供的编码不一致时，有可能你错误的估计了源文件的编码方式，导致转换后就是乱码。这种情况下，encodingConversion 会终止执行。

同时，你可以通过以下命令来查看使用说明：
```cmd
java -jar encodingConversion-1.0.jar --help
```
```result

Usage: java -jar encodingConversion-x.y.z.jar [options]

Global Options
  -h, --help                                   Displays this information
  -p, --path                                   待转换编码的文件所在的目录
  -f, --file-encoding                          待转换编码的文件的原始编码，若未提供，则采取自动探测原始编码
  -t, --convert-to                             待转换编码的文件的要转换成的编码
```
