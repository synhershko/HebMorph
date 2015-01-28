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
In this guide, I'll be using 'example' as my <b> instanceDir </b>, and 'collection1' as my <b> core </b>. Replace those with yours. If you don't know them, you can easily find them by opening your browser and navigating to http://localhost:8983/solr/#/~cores (replace with your host:port).

Add the following line to solrconfig.xml which can be found at solr/<i>collection1</i>/conf directory, within your solr-instanceDir. For example [solr home]/<i>example</i>/solr/<i>collection1</i>/conf/solrconfig.xml.
```
    <lib path="[hebmorph_path]" />
```
Replace [hebmorph_path] with the location of your hebmorph-lucene.jar

Now, you need to place your dictionary files in one of the predefined locations. Currently, you should place the dictionary files in the home of you solr <b> instanceDir </b> folder. That is, [solr home]/<i>example</i>/ folder.

You need to define your schemas. If you do not know how schemas work in Solr, refer to http://wiki.apache.org/solr/SchemaXml.
Open your schema file ([solr home]/<i>example</i>/solr/<i>collection1</i>/conf/schema.xml). The easiest way to quickly add your desired analyzers is to replace the "text-general" fieldtype. That way, all default text values will be analyzed using hebmorph.
Locate
```
	<fieldType name="text_general" class="solr.TextField" positionIncrementGap="100">
      <analyzer type="index">
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
        <!-- in this example, we will only use synonyms at query time
        <filter class="solr.SynonymFilterFactory" synonyms="index_synonyms.txt" ignoreCase="true" expand="false"/>
        -->
        <filter class="solr.LowerCaseFilterFactory"/>
      </analyzer>
      <analyzer type="query">
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
        <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true"/>
        <filter class="solr.LowerCaseFilterFactory"/>
      </analyzer>
    </fieldType>
```

And replace it with
```
    <fieldType name="text_general" class="solr.TextField">
		<analyzer type="index" class="org.apache.lucene.analysis.hebrew.HebrewIndexingAnalyzer" /> 
		<analyzer type="query" class="org.apache.lucene.analysis.hebrew.HebrewQueryAnalyzer" />
    </fieldType>
```

HebMorph contains 4 different hebrew analyzers for you to choose from:

HebrewIndexingAnalyzer: <description>

HebrewExactAnalyzer: <description>

HebrewQueryAnalyzer: <description>

HebrewQueryLightAnalyzer: <description>

That's it. Restart Solr and your chosen analyzer(s) should automatically apply when querying or indexing new data.

In order to see if the analyzer works, browse to the Analysis section in solr-admin (http://localhost:8983/solr/#/<i>collection1</i>/analysis). Write any phrase ("בדיקת עברית"), select any fieldname which represents a text ("content","title", etc.) and analyze. Then, choose the fieldtype "text_en" and analyze. You should see the clear differences.

It is released to the public licensed under the GNU Affero General Public License v3. See the LICENSE file included in this distribution. Note that not only the programs in the distribution, but also the
dictionary files and the generated word lists, are licensed under the AGPL.
There is no warranty of any kind for the contents of this distribution.
