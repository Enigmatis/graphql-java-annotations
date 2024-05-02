RELEASE DOCUMENTATION
---------------------

Here are the steps to produce a release of the project:

1. Check the gradle.properties file to make sure that the version number has been updated
2. In the Github release UI, create a new tag with the following naming convention vXX.X and make sure you generate the release notes. 
3. Create the release using the create release button
4. Wait for the build action to complete successfully
5. Launch the "Publish" action manually and wait for it to complete
6. Check at the following URL that the new release was deployed : 

    https://repo1.maven.org/maven2/io/github/graphql-java/graphql-java-annotations/

   It might take some time for the release to appear
7. Update this document if anything was missing or wasn't clear   
8. Once everything is properly deployed, update the gradle.properties file to the next planned version, either directly (if that is the only change), or in a PR if other changes need to be done at the same time.
9. Announce the release on your favorite communication channels.
