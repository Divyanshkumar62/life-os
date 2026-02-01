import { BrowserRouter } from 'react-router-dom';
// import { AppShell } from './AppShell'; 
// We will uncomment when AppShell is created. For now, simple placeholder.

function App() {
    return (
        <BrowserRouter>
            <div className="min-h-screen bg-neutral-900 text-white flex items-center justify-center">
                <h1 className="text-3xl font-bold text-red-500">LifeOS System Initialized</h1>
            </div>
        </BrowserRouter>
    );
}

export default App;
