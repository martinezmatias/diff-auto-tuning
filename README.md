# auto-tuning

To generate GumTree 3 jar:

From core and jdt build.


description = 'GumTree core module.'

dependencies {
	compile 'com.github.mpkorstanje:simmetrics-core:3.2.3'
	compile 'net.sf.trove4j:trove4j:3.0.3'
	compile 'com.google.code.gson:gson:2.8.2'
	compile 'org.jgrapht:jgrapht-core:1.0.1'
}

allprojects {
	gradle.projectsEvaluated {
		tasks.withType(JavaCompile) {
			options.compilerArgs << "-Xlint:unchecked" << "-Xlint:deprecation"
		}
	}

}

task fatJar(type: Jar) {
   // manifest {
    //    attributes 'Implementation-Title': 'Gradle Jar File Example',
   //             'Implementation-Version': "vmm"
   // }
    baseName = project.name + '-all'
    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}

//Get dependencies from Maven central repository
repositories {
    mavenCentral()
}
