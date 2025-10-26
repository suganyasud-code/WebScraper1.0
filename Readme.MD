Prerequisites
Java 17+
Maven 3.8+
Internet connectivity (to access Jira and OpenAI/DeepSeek endpoints)
📦 Installation



Clone the repository:

git clone https://github.com/yourusername/WebScraper1.0.git
cd WebScraper1.0




Build the project:

mvn clean install




Run the scraper:

mvn exec:java -Dexec.mainClass="com.example.WebScraper1.0.JiraScraperMain"

🔑 Configuration



Create a config.properties file under src/main/resources with:

jira.baseUrl=https://issues.apache.org/jira/rest/api/2
jira.project=HADOOP
jira.maxResults=50
openai.api.key=YOUR_API_KEY
openai.model=gpt-3.5-turbo


🧠 AI Summarization Example
Example prompt handled by OpenAISummarizer:
AI Output

Issue Summary: A DataNode in Hadoop was incorrectly advertising 'localhost' as its network address.
Impact: Major, as it affects distributed connectivity.
Resolution: Closed after correcting configuration logic.