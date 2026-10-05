### Installing Jenkins
Below is a complete, step‑by‑step guide to installing Jenkins in Docker on Windows, 
then configuring it for your RabbitMQ Spring Boot microservices project.

I’ll use PowerShell commands. 
If you prefer CMD or WSL, the Docker commands are the same; only the line‑continuation syntax changes.

## 1. Prerequisites
   Make sure you have:

Docker Desktop installed and running.

Git installed (optional but recommended).

A browser (Edge/Chrome/Firefox).

Check Docker:

```powershell
docker version
```

You should see client and server information. If not, start Docker Desktop and wait until it says “Docker Desktop is running”.

## 2. Create a Directory for Jenkins Data
   Jenkins needs persistent storage so your jobs and configuration survive container restarts.

Create a folder, for example:

```powershell
mkdir C:\jenkins_home
```
This will be mounted into the container at /var/jenkins_home.

## 3. Create a Docker Network (Optional but Clean)
   This lets Jenkins and other containers (like RabbitMQ) communicate if needed.

```powershell
docker network create jenkins
```
You can skip this and use the default bridge network, but using a named network is cleaner.

## 4. Pull the Jenkins Docker Image
   Use the official LTS image with a supported JDK. Current Jenkins LTS images are built with JDK 21.

```powershell
docker pull jenkins/jenkins:lts-jdk21
```

Check the image:

```powershell
docker images jenkins/jenkins
```

You should see something like:
``` text
REPOSITORY        TAG                 IMAGE ID
jenkins/jenkins   lts-jdk21           ...
```
## 5. Run Jenkins in Docker
   Run the container with:

Port 8080 for the web UI.

Port 50000 for Jenkins agents.

A volume for /var/jenkins_home.

Access to the Docker socket so Jenkins can run Docker commands (needed for your Docker pipeline).

## 5.1 Basic Command
```powershell
docker run -d `
  --name jenkins `
--restart unless-stopped `
  --network jenkins `
-p 8080:8080 `
  -p 50000:50000 `
-v C:\jenkins_home:/var/jenkins_home `
  -v //var/run/docker.sock:/var/run/docker.sock `
jenkins/jenkins:lts-jdk21
```
Explanation of important flags:

-d – run in background (detached).

--name jenkins – container name.

--restart unless-stopped – auto‑restart if it crashes or Docker restarts.

--network jenkins – use the network you created.

-p 8080:8080 – map host port 8080 to container port 8080.

-p 50000:50000 – agent communication port.

-v C:\jenkins_home:/var/jenkins_home – persistent storage.

-v //var/run/docker.sock:/var/run/docker.sock – give Jenkins access to Docker.

On some Windows setups, the Docker socket path may differ. If //var/run/docker.sock doesn’t work, try:

```powershell
-v /var/run/docker.sock:/var/run/docker.sock
```
or (for Docker Desktop with WSL2 backend) ensure Docker Desktop’s settings allow socket access.

## 6. Verify the Container Is Running
```   powershell
   docker ps
   ```
   You should see:

```text
CONTAINER ID   IMAGE                     NAMES
...            jenkins/jenkins:lts-jdk21 jenkins
```
Check logs:

```powershell
docker logs jenkins
```
You should see Jenkins startup messages.

## 7. Open Jenkins in the Browser
   Open:

```text
http://localhost:8080
```
![Jenkins started](./images/Jenkins%201.jpeg)


You’ll see the Jenkins setup page asking for an initial admin password.

## 8. Get the Initial Admin Password
   The password is stored inside the container.

Run:

```powershell
docker exec jenkins `
cat /var/jenkins_home/secrets/initialAdminPassword
```
Copy the printed password.

Paste it into the Administrator password field on the Jenkins page, then click Continue.
![Jenkins started](./images/Jenkins%202.jpeg)

## 9. Install Plugins
   You’ll see two options:

Install suggested plugins

Select plugins to install

Choose Install suggested plugins.

Jenkins will download and install a set of common plugins. This may take a few minutes.

![Jenkins started](./images/Jenkins%203.jpeg)

## 10. Create the First Admin User
    After plugin installation, you’ll be asked to create the first admin user.

![Jenkins started](./images/Jenkins%204.jpeg)

Fill in:

Username: e.g. admin

Password: choose a strong password

Name: your name

Email address: your email

Click Save and Continue.

## 11. Configure Jenkins URL
    Next screen: Jenkins URL.

Usually the default is fine:

```text
http://localhost:8080/
```
Click Save and Finish.

Then click Start using Jenkins.

![Jenkins started](./images/Jenkins%205.jpeg)

You’re now logged into Jenkins.

![Jenkins started](./images/Jenkins%206.jpeg)

![Jenkins started](./images/Jenkins%207.jpeg)

Click Settings

![Jenkins started](./images/Jenkins%208.jpeg)

## 12. Install Additional Plugins for Your Project
    You need extra plugins for Docker, Maven, Cucumber, etc.

Click Manage Jenkins (left menu).

Click Plugins.

Click Available plugins.

Search and install these plugins:

Pipeline

Git

GitHub Integration (if using GitHub)

Maven Integration

JUnit

Docker Pipeline

Docker Commons

Docker Workflow

Credentials Binding

SSH Agent

Cucumber Reports

HTML Publisher

Timestamper

You can search each by name, tick the checkbox, then click Install.

Restart Jenkins if requested.

## 13. Verify Docker Access from Jenkins
    Because you mounted the Docker socket, Jenkins should be able to run Docker commands.

# 13.1 Create a Test Pipeline Job
Click New Item.

Enter name: docker-test.

Select Pipeline.

Click OK.

In Pipeline → Pipeline script, paste:

```groovy
pipeline {
agent any

    stages {
        stage('Docker version') {
            steps {
                sh 'docker version'
            }
        }

        stage('Docker info') {
            steps {
                sh 'docker info'
            }
        }
    }
}
```
Click Save.

Click Build Now.

Click the build number, then Console Output.

You should see docker version and docker info output.

If you get “docker: command not found” or permission errors, the Docker socket mount may be incorrect. In that case, adjust the docker run command and recreate the container.

## 14. (Optional) Increase Memory for Jenkins
    If you see OutOfMemory errors or slow performance, you can increase memory:

Stop and remove the container:

```powershell
docker stop jenkins
docker rm jenkins
```
Run again with memory limits, for example 2 GB:

```powershell
docker run -d `
  --name jenkins `
--restart unless-stopped `
  --network jenkins `
-p 8080:8080 `
  -p 50000:50000 `
-v C:\jenkins_home:/var/jenkins_home `
  -v //var/run/docker.sock:/var/run/docker.sock `
--memory=2g `
jenkins/jenkins:lts-jdk21
```
Adjust --memory as needed.

## 15. Access Jenkins Long‑Term
    Your Jenkins data is stored in:

```text
C:\jenkins_home
```
As long as you keep this folder and recreate the container with the same volume mount, your jobs, credentials, and configuration will persist.

To stop Jenkins:

```powershell
docker stop jenkins
```
To start again:

```powershell
docker start jenkins
```
To view logs:

```powershell
docker logs -f jenkins
```
## 16. Next Steps for Your Project
    Now that Jenkins is installed:

Add Docker Hub and SSH credentials (as described earlier).

Create a Pipeline job pointing to your Git repo with Jenkinsfile.

Run builds, view test reports, and configure deployment.





