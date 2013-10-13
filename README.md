HebMorph is an open-source effort for making Hebrew properly searchable by various IR software libraries, while maintaining decent recall, precision and relevancy in retrievals. All code and files are released under the GNU Affero General Public License version 3.

More details at http://code972.com/HebMorph

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
			<td>1.2.0-SNAPSHOT (master)</td>
			<td>4.5.0</td>
			<td>0.90.6 -> master</td>
			<td>(not yet released)</td>
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
            <version>1.1</version>
            <scope>compile</scope>
        </dependency>
```

Direct reference to hebmorph-core is only needed if you need to use internal HebMorph structures:

```
        <dependency>
            <groupId>com.code972.hebmorph</groupId>
            <artifactId>hebmorph-core</artifactId>
            <version>1.1</version>
            <scope>compile</scope>
        </dependency>
```

## License

HebMorph is copyright (C) 2010-2013, Itamar Syn-Hershko.
HebMorph currently relies on Hspell, copyright (C) 2000-2013, Nadav Har'El and Dan Kenigsberg (http://hspell.ivrix.org.il/).

It is released to the public licensed under the GNU Affero General Public License v3. See the LICENSE file included in this distribution. Note that not only the programs in the distribution, but also the
dictionary files and the generated word lists, are licensed under the AGPL.
There is no warranty of any kind for the contents of this distribution.
