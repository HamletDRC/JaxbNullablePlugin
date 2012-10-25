JaxbNullablePlugin
==================

JAXB2 Plugin that Adds @Nullable and @Nonnull annotations to generated classes.

The javax.annotation.@Nonnull annotation is applied to getter and setter methods where:

 * The source XML Attribute is marked "required"
 * The source XML Element is marked minOccurs="1"
 * The source XML Element produces a java.util.List type

The javax.annotation.@Nullable annotation is applied to getter and setter methods where:

 * The source XML Attribute is marked "optional"
 * The source XML Element is marked minOccurs="0"
 * The source XML nullity cannot be determined (the default)

Examples
==================

An XML schema element with minOccurs="0" will be annotated with @Nullable:

    <xsd:element name="my_field" type="xsd:string" minOccurs="0" maxOccurs="1"/>
    ...
    @Nullable
    public String getMyField() {
      return myField;
    }
    public void setMyField(@Nullable String value) {
      myField = value;
    }

An XML schema element with minOccurs="1" will be annotated with @Nonnull

    <xsd:element name="my_field" type="xsd:string" minOccurs="1" maxOccurs="1"/>
    ...
    @Nonnull
    public String getMyField() {
      return myField;
    }
    public void setMyField(@Nonnull String value) {
      myField = value;
    }

Downloading
==================

This Jar is not yet in Maven central. Please be patient. You can download all the releases from here:

 * [https://github.com/HamletDRC/JaxbNullablePlugin/tree/master/releases](https://github.com/HamletDRC/JaxbNullablePlugin/tree/master/releases)

Using from Maven
==================

I use this JAXB plugin from the Maven Jaxb2 plugin [http://java.net/projects/maven-jaxb2-plugin](http://java.net/projects/maven-jaxb2-plugin/)

You just need to modify your jaxb2 Plugin's Configuration element and the dependency on my Jar:

    <plugin>
      <groupId>org.jvnet.jaxb2.maven2</groupId>
      <artifactId>maven-jaxb2-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <args>
          <arg>-XNullable</arg>
        </args>
        <plugins>
          <plugin>
            <groupId>com.github.jaxb2.plugin</groupId>
            <artifactId>JaxbNullablePlugin</artifactId>
            <version>1.0.0</version>
          </plugin>
        </plugins>
      </configuration>
    </plugin>


Dependencies
==================
The JaxbNullablePlugin does not have any dependencies. It assumes that you are building with a project
that has jaxb2 and the javax.annotations.* annotations.

**If you are using Java SE 6, then you will not have the @Nullable and @Nonnull annotations by default.**

The easiest place to find them is in the FindBugs annotations Jar. Be sure to put this on your classpath.

You can put it on the plugin classpath:

    <plugin>
      <groupId>org.jvnet.jaxb2.maven2</groupId>
      <artifactId>maven-jaxb2-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <args>
          <arg>-XNullable</arg>
        </args>
        <plugins>
          <plugin>
            <groupId>com.github.jaxb2.plugin</groupId>
            <artifactId>JaxbNullablePlugin</artifactId>
            <version>1.0.0</version>
          </plugin>
        </plugins>
      </configuration>
      <dependencies>
        <!-- Needed for the annotation processing -->
        <dependency>
          <groupId>com.google.code.findbugs</groupId>
          <artifactId>annotations</artifactId>
          <scope>compile</scope>
          <version>2.0.0</version>
        </dependency>
      </dependencies>
    </plugin>

Or you can just declare the dependency in your normal dependencies section:

    <dependency>
      <groupId>com.google.code.findbugs</groupId>
      <artifactId>annotations</artifactId>
      <scope>compile</scope>
      <version>2.0.0</version>
    </dependency>


Limitations/Known Issues
==================

XML Elements of type "ref" are treated as @Nullable. We *should* be able to read the Nillable=true/false
 attribute and annotate the methods correctly, but it is not so easy to do.

I do not need this bug fixed, so I am waiting until someone asks to fix it.

 **If you need this feature** then just please vote or leave a comment
 on [https://github.com/HamletDRC/JaxbNullablePlugin/issues/1](https://github.com/HamletDRC/JaxbNullablePlugin/issues/1)