# Stigmergy ODEA (Open Distributed Edge Agents)

The active development is in the [nearby-connections](https://github.com/Open-Distributed-Edge-Agents/EdgeGenAI/tree/nearby-connections) feature branch.

## Overview

Stigmergy ODEA is a decentralized, agentic system designed for high-stakes tactical operations on edge devices. It leverages the multi-modal capabilities of Google's groundbreaking Gemma 3B model to create a collective intelligence highly resistant to malfunctions and adversarial attacks.

Deployed on a fleet of tiny Android devices (e.g., drones), the system operates as a hierarchical team with a designated **Commander** and multiple **Subordinates**. Each unit runs its own local agent, participating in a group chat to collaboratively perceive, plan, and act. This architecture is built for strategic decision-making and guidance in environments where real-time, centralized control is infeasible or too risky.

The agents communicate via **Android Nearby Connections**, forming a resilient mesh network that can adapt to changing conditions on the fly.

## Core Concepts

*   **Collective Intelligence:** The system's strength lies in the emergent intelligence of the group. By sharing observations and intent through stigmergic communication, the agents can uncover hidden threats, adapt to environmental changes, and maintain mission alignment even when individual units are compromised.
*   **Decentralized Agency:** Each drone is equipped with a powerful local agent, allowing for autonomous operation and reducing reliance on a single point of failure.
*   **Commander/Subordinate Hierarchy:** The Commander provides strategic direction, issuing broadcast commands and requesting status reports to maintain operational tempo. However, the system is designed to survive the loss of a commander, with protocols in place to elect a new one.

## Key Features

*   **High Resilience & Fault Tolerance:**
    *   **Commander Loss:** The agent swarm can detect the loss of a commander and dynamically appoint a new one, ensuring mission continuity.
    *   **Decentralized Operation:** The system avoids single points of failure, as intelligence is distributed across all agents.
*   **Advanced Security:**
    *   **Prompt Injection Resistance:** The agents' intelligence and mission context help them identify and resist malicious prompts intended to derail their objectives.
    *   **Impersonation Defense:** The system is designed to detect and reject commands from unauthorized or impersonated devices.
*   **Multi-Modal Perception:**
    *   Leveraging the **Gemma 3B** model, agents can process both text and visual data to achieve a deeper understanding of their environment. This allows them to recognize discrepancies between expected and perceived mission targets, flagging potential issues or threats.
*   **Edge-Optimized:**
    *   Designed to run on resource-constrained tiny Android devices with processing power significantly less than a Jetson Orin Nano.
    *   Communication is handled efficiently through the low-power, high-bandwidth capabilities of Android Nearby Connections.

## Use Cases

The primary use case for Stigmergy ODEA is to support **first responders** in assessing and navigating post-catastrophe environments. Deploying a swarm of ODEA-enabled drones can provide critical situational awareness after events like:

*   Earthquakes
*   Forest Fires
*   Floods and Hurricanes

The collective intelligence can map disaster areas, identify survivors, and detect ongoing hazards, all while resisting the chaotic and unpredictable conditions of the environment.

## Technology Stack

*   **Generative AI:** Google Gemma 3B
*   **Platform:** Android
*   **Communication:** Android Nearby Connections
*   **Architecture:** Decentralized Multi-Agent System

## Gemma 3n Challenge

This project was submitted to the [Gemma 3n Challenge on Kaggle](https://www.kaggle.com/competitions/google-gemma-3n-hackathon/).

## Future Plans

Our roadmap includes the following feature additions:

*   **Introduce Koog for Full Agentic Capabilities:** Integrate the Koog framework to enable more complex, goal-oriented agentic behaviors.
*   **Explore Conversational Consensus Algorithms:** Research and implement algorithms that allow agents to reach a consensus through dialogue, improving collective decision-making.
*   **Leverage Vector Store for Mission Detail Retrieval:** Utilize a vector store for efficient retrieval of mission-critical information, enhancing agent knowledge and responsiveness.
*   **Log Interactions on a Crypto Ledger:** Implement a secure, immutable ledger for all agent interactions, providing a transparent and tamper-proof audit trail.
*   **Add Prompt Guard:** Integrate a prompt guarding mechanism, such as Llama Guard 2, to further enhance security against adversarial attacks.

For more details, please see our [GitHub Issues](https://github.com/Open-Distributed-Edge-Agents/EdgeGenAI/issues).

## Credits

This project is a fork of the official [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery) demo app and builds upon its foundation. We are grateful to the original authors for their work.

## Contributing

We welcome contributions! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to get started.

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
