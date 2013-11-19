tzatziki
========

Tzatziki is a Maven plugin which aims to maintain a link between a Cucumber-JVM test report and a defect tracking system. This connection allows you to auto generate defects on test failure, include a reference to a defect in a test report and introduces the concept of expected failures.
At present, this is a proof of concept only and is NOT PRODUCTION READY.

Defect trackers currently supported:
* Jira

What does it do?
================

* Parses a Cucumber-JVM JSON report and generates a unique ID from the step name.
* Identifies failing tests in the report
* Searches your defect tracker for matching defects
    * If a defect is found, determine whether the defect is expected or newly created (based on defect type)
    * If a defect is not found, create a new defect

Roadmap:

* Update Cucumber-JVM report to embed link to matching defect on test step
* Update Cucumber-JVM report to ignore expected failures
* Highlight unexpected passes (passing tests which have a related open defect)

Reasoning
=========

At present, the process of managing links between failing tests and defects is manual.
The main driver for this plugin is introduction of expected failure. With a set of tests running on CI, we expect the tests to always pass and remain green. Failures should be addressed immediately.
If this is the case then the process works fine, however it is common (particularly with acceptance testing) to have a valid, failing test which relates to a low priority defect.
Where that defect will not be addressed immediately, we are left with failing tests on CI which lead to other issues being less obvious.
Normally you are left with two options for dealing with this:
* Update the test to match current behaviour, with a note on the related defect to modify it back as part of fix verification.
* Ignore the test until defect is fixed.

With either approach, you can use Cucumber tagging to include a reference to the defect with the test, but this process is manual and difficult to track.

If we introduce known defects into our workflow, our test report becomes more detailed. Disregarding known defects means our test runs keep passing so that new failures are obvious.
Retaining those known failures in the report with a reference to the related defect allows us to keep track of the functional impact of open tests.

At present the difference between a new and known failure is tracked using the defect type. Create a custom type (e.g. auto-generated bug) in defect tracker and tell the plugin to create defects as this type.
Defects which are matched to a test failure are treated as expected if the type does NOT match "new defect type", on the assumption that a manual step would be required to review and transition valid defects to a standard defect type.

Build and use
=============
Git clone
mvn install

To use in a maven project, include the following in your pom:

            <plugin>
                <groupId>com.opencredo</groupId>
                <artifactId>tzatziki</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <phase>post-integration-test</phase>
                        <goals><goal>linkdefects</goal></goals>
                    </execution>
                </executions>
                <configuration>
                    <reportFile>path/to/json/report</reportFile>
                    <jiraSettings>
                        <property>
                            <name>jiraServerAddress</name>
                            <value>your-jira-server</value>
                        </property>
                        <property>
                            <name>jiraUserName</name>
                            <value>your-jira-username</value>
                        </property>
                        <property>
                            <name>jiraUserPassword</name>
                            <value>your-jira-password</value>
                        </property>
                        <property>
                            <name>jiraProjectKey</name>
                            <value>your-jira-project-key</value>
                        </property>
                        <property>
                            <name>jiraNewDefectType</name>
                            <value>your-jira-custom-issue-type</value>
                        </property>
                    </jiraSettings>
                </configuration>
            </plugin>

