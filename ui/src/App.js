import React, { useState } from 'react';
import logo from './logo.svg';
import './App.css';
import Policies from './Policies';
import CreateOpinion from './CreateOpinion';
import PolicyDetails from './PolicyDetails';

function App() {
  const [view, setView] = useState('policies');
  const [selectedPolicyId, setSelectedPolicyId] = useState(null);

  const navigateToPolicy = (id) => {
    setSelectedPolicyId(id);
    setView('policy-details');
  };

  const renderView = () => {
    switch (view) {
      case 'policies':
        return <Policies onPolicyClick={navigateToPolicy} />;
      case 'create-opinion':
        return <CreateOpinion />;
      case 'policy-details':
        return <PolicyDetails policyId={selectedPolicyId} onBack={() => setView('policies')} />;
      default:
        return <Policies onPolicyClick={navigateToPolicy} />;
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Popular Vote System</h1>
        <nav>
          <button onClick={() => setView('policies')} style={{ marginRight: '10px' }}>Policies</button>
          <button onClick={() => setView('create-opinion')}>Create Opinion</button>
        </nav>
      </header>
      <main>
        {renderView()}
      </main>
    </div>
  );
}

export default App;
