# compressor

compressor is a Java library which enables you to compress your JavaScript and CSS files while building your project through maven.

## Integration

First, download this library and paste it into your local maven repository.
Create the below folder structure in your local maven repository.

`.m2/repository/com/github/raman002/compressor/1.0`

After the above folder structure is created, paste below `jar` files located in the `dist` folder to `1.0` folder.

```
compressor-1.0.jar
compressor-1.0-sources.jar
```


Insert below dependency tag into your pom.xml

```
<dependency>
    <groupId>com.github.raman002</groupId>
    <artifactId>compressor</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

Add below tag to your pom.xml to enable the automated compression for your JS and CSS resources.
If you already have a `<build>` tag then you can simply copy `<plugins>` tag.

```
<build>
 <plugins>
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.6.0</version>
        <executions>
            <execution>
                <id>compress</id>
                <phase>clean</phase>
                <configuration>
                    <mainClass>com.github.raman002.compressor.main.Minify</mainClass>
                </configuration>
                <goals>
                    <goal>java</goal>
                </goals>
            </execution>

            <execution>
                <id>revert</id>
                <phase>package</phase>
                <configuration>
                    <mainClass>com.github.raman002.compressor.main.Minify</mainClass>
                    <arguments>
                        <argument>revertFiles</argument>
                    </arguments>
                </configuration>
                <goals>
                    <goal>java</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
 </plugins>
</build>
```
Once you have added above tags and started building your project you can use `mvn clean package` or `mvn clean install`, 
Make sure to validate your project archive after building your project if the JS and CSS resources have been minified.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)