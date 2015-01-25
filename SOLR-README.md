HebMorph is an open-source effort for making Hebrew properly searchable by various IR software libraries, while maintaining decent recall, precision and relevancy in retrievals. All code and files are released under the GNU Affero General Public License version 3.

More details at http://code972.com/HebMorph


Tutorial for integrating HebMorph with Elasticsearch can be found here http://code972.com/blog/2013/08/129-hebrew-search-with-elasticsearch-and-hebmorph

## Solr installation guide

## Prerequisites
* Solr (Versions supported: 4.10.x and above).
* A Solr Collection.
( If you do not have these already set-up, please refer to http://lucene.apache.org/solr/resources.html#tutorials )
* hebmorph-lucene.jar (can be downloaded from ***).
* Hebrew dictionary files. 


## Integrating HebMorph
Add the following line to solrconfig.xml which can be found at solr/conf directory, within your solr-collection. Replace [hebmorph_path] with the location of your hebmorph-lucene.jar
```
    <lib path="[hebmorph_path]" />
```
You need to define your schemas. If you do not know how schemas work in Solr, refer to http://wiki.apache.org/solr/SchemaXml.
HebMorph contains 4 different hebrew analyzers. Those analyzers are already customized to work as are, and all we need to do in order to use them is to add the follwing line to the field-type you want to use hebmorph on.
```
	<analyzer type="index" class="org.apache.lucene.analysis.hebrew.HebrewIndexingAnalyzer" /> 
	or
	<analyzer type="query" class="org.apache.lucene.analysis.hebrew.HebrewQueryAnalyzer" />
```
Where HebrewIndexingAnalyzer and HebrewQueryAnalyzer are the analyzers chosen. Analyzers are:
HebrewIndexingAnalyzer: <description>
HebrewExactAnalyzer: <description>
HebrewQueryAnalyzer: <description>
HebrewQueryLightAnalyzer: <description>

That's it. Restart Solr and your chosen analyzer(s) should automatically apply when querying or indexing new data.

It is released to the public licensed under the GNU Affero General Public License v3. See the LICENSE file included in this distribution. Note that not only the programs in the distribution, but also the
dictionary files and the generated word lists, are licensed under the AGPL.
There is no warranty of any kind for the contents of this distribution.
