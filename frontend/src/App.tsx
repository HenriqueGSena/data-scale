import {UploadPage} from "./pages/UploadPage.tsx";
import {UpdateList} from "@/list/UpdateList.tsx";


function App() {
  return (
      <div className="min-h-screen bg-background text-foreground">
        <header className="border-b">
          <div className="mx-auto max-w-6xl px-4 py-4">
            <h1 className="text-lg font-semibold">DataScale</h1>
          </div>
        </header>
        <main className="mx-auto max-w-6xl px-4 py-8 space-y-8">
            <UploadPage />
            <UpdateList />
        </main>
      </div>
  )
}

export default App
