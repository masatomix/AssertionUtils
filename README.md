# AssertionUtils


usage:
java -jar AssertionUtils.jar  -i data\shin -o data\gen -logic nu.mine.kino.assertionutils.CSVAssertionLogic3

-i： 期待値ディレクトリ(カレントディレクトリからの相対パスもしくは絶対パスで指定)
-o： 検証データディレクトリ(同上)

-logicの ロジック名はとりあえず上記の通りで。

また、クラスパス上もしくはjarのあるディレクトリに
excludeColumns.properties
というファイルを置いてください。

xxx.tsv=0,1,2  
yyy.tsv = 2,4  

など除外カラム番号(0始まり)で指定すると、そのカラムは除外して比較。
また、それぞれのディレクトリに xxx.tsv_modified などというファイルも出力します。
これらは、除外カラムを除去した後のファイルです。このツールで直接的にDIFFが取れない場合、その除去されたファイルを用いて他ツールなどで比較処理を行ってください。

