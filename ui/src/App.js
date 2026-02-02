import React, { useState } from 'react';
import logo from './logo.svg';
import './App.css';
import Policies from './Policies';
import CreateOpinion from './CreateOpinion';

function App() {
  const [view, setView] = useState('policies');

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
        {view === 'policies' ? <Policies /> : <CreateOpinion />}
      </main>
    </div>
  );
}

export default App;
