HebMorph is an open-source effort for making Hebrew properly searchable by various IR software libraries, while maintaining decent recall, precision and relevancy in retrievals. All code and files are released under the GNU Affero General Public License version 3.

More details at http://code972.com/HebMorph

[![Build Status](https://drone.io/github.com/synhershko/HebMorph/status.png)](https://drone.io/github.com/synhershko/HebMorph/latest)  [![Build Status](https://travis-ci.org/synhershko/HebMorph.svg?branch=master)](https://travis-ci.org/synhershko/HebMorph)

## Lucene / Elasticsearch compatibility

<table>
	<thead>
		<tr>
			<td>hebmorph-lucene version</td>
			<td>Lucene version</td>
			<td>Elasticsearch version</td>
			<td>Release date</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>2.3.x</td>
			<td>5.4.x</td>
			<td>2.2.x</td>
			<td>4/2/2016</td>
		</tr>
		<tr>
			<td>2.2.x</td>
			<td>5.3.x</td>
			<td>2.0.x -> 2.1.x</td>
			<td>4/2/2016</td>
		</tr>
		<tr>
			<td>2.1.x</td>
			<td>4.10.4</td>
			<td>1.6 -> 1.7.x</td>
			<td>4/2/2016</td>
		</tr>
		<tr>
			<td>2.0.x</td>
			<td>4.10.x</td>
			<td>1.4.x, 1.5.x</td>
			<td>24/3/2015</td>
		</tr>
		<tr>
			<td>1.5.0</td>
			<td>4.9.0</td>
			<td>1.3.x</td>
			<td>9/9/2014</td>
		</tr>
		<tr>
			<td>1.4.x</td>
			<td>4.8.x</td>
			<td>1.x -> 1.2.x</td>
			<td>August 2014</td>
		</tr>
		<tr>
			<td>1.3.x</td>
			<td>4.6.x</td>
			<td>0.90.8 -> 0.90.13</td>
			<td>June 2014</td>
		</tr>
		<tr>
			<td>1.2.0</td>
			<td>4.5.x</td>
			<td>0.90.6, 0.90.7</td>
			<td>10/11/2013</td>
		</tr>
    <tr>
			<td>1.1.0</td>
			<td>4.4.0</td>
			<td>0.90.3 -> 0.90.5</td>
			<td></td>
		</tr>
		<tr>
			<td>1.0.0</td>
			<td>&lt;= 4.3.0</td>
			<td>&lt;= 0.90.2</td>
			<td></td>
		</tr>
	</tbody>
</table>

Tutorial for integrating HebMorph with Elasticsearch can be found here http://code972.com/blog/2013/08/129-hebrew-search-with-elasticsearch-and-hebmorph

## Get it from Maven Central

For the analyzer support, get hebmorph-lucene:

```
        <dependency>
            <groupId>com.code972.hebmorph</groupId>
            <artifactId>hebmorph-lucene</artifactId>
            <version>2.0.2</version>
            <scope>compile</scope>
        </dependency>
```


## Lucene.NET compatibility

The .NET version of the library is compatible with Lucene.NET version 3.0.3, but has some known bugs that were fixed in the Java version and haven't been ported back yet.

## License

HebMorph is copyright (C) 2010-2015, Itamar Syn-Hershko.
HebMorph currently relies on Hspell, copyright (C) 2000-2013, Nadav Har'El and Dan Kenigsberg (http://hspell.ivrix.org.il/).

It is released to the public licensed under the GNU Affero General Public License v3. See the LICENSE file included in this distribution. Note that not only the programs in the distribution, but also the
dictionary files and the generated word lists, are licensed under the AGPL.
There is no warranty of any kind for the contents of this distribution.
