# graal-auto-reflection
Automatically scan and register classes for reflection using a provider interface 

##Installation
1. Add Jitpack to repos in gradle.build.kts
    
    ```kotlin
    repositories {
       maven {
           url = uri("https://jitpack.io")
       }
    }
    ```

2. Add dependency
    ```kotlin
    dependencies {
        compile("com.github.richarddd:graal-auto-reflection:-SNAPSHOT")
    }
    ```

##Usage
````kotlin
@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
class ReflectionData : ReflectionProvider {
    override fun packages(classGraph: ClassGraph) =
        Arrays.asList(
            "com.mongodb.internal.connection",
            "com.mongodb.crypt.capi",
            "org.litote.kmongo.pojo",
            "kotlin.reflect"
        )
    
    override fun classNames(classGraph: ClassGraph) =
            Arrays.asList("com.example.MyClass")

    override fun classes(classGraph: ClassGraph) =
        Arrays.asList(UnixServerAddress::class.java)
}
````
