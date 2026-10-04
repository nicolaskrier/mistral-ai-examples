# Mistral AI Examples

*Spring AI examples using Mistral AI as AI model provider.*

## 🛠️ Pre-Requisites

- Java 25
- Maven 4
- Mistral AI API key
- Qdrant Cloud AI API key (for the RAG example)

## 📚 Use Cases

- **Chat Client**: Example covering the following features:
    - Thinking model,
    - JSON structured output,
    - In-memory chat memory,
    - Request and response message logging.
- **RAG**: Example covering the following features:
    - Embedding model,
    - JSON data with metadata reading to add documents to Qdrant vector store only if the stored collection is empty,
    - Question and answer with a filtered search limiting data retrieval from the vector store,
    - JSON structured output,
    - In-memory chat memory,
    - Request and response message logging.
- **Tools**: Example covering the following features:
    - Custom tools to fetch current date time and to search pope either by date or by pontiff number,
    - JSON structured output,
    - In-memory chat memory,
    - Request and response message logging.
- **MCP**: Example covering the following features:
    - Stateless Streamable-HTTP MCP servers offering tools to fetch current date time and to search pope either by date or by pontiff number,
    - MCP client using these two MCP servers,
    - JSON structured output,
    - In-memory chat memory,
    - Request and response message logging.

## 🧠 Models

### Chat Models

- Mistral Medium Latest.

### Embedding Models

- Mistral Embed.

## 🗃️ Vector Stores

- **Qdrant**
