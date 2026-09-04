import React from 'react';
import { 
  MessageCircle, 
  FileText, 
  FileUp, 
  Library, 
  BookOpen, 
  ListChecks,
  Mic,
  Settings 
} from 'lucide-react';

export const sidebarData = [
  {
    title: "Legal Query Chatbot",
    IconComponent: MessageCircle,
    path: "/dashboard/chatbot"
  },
  {
    title: "Document Generator",
    IconComponent: FileText,
    path: "/dashboard/document-generator"
  },
  {
    title: "Case Law Summarizer",
    IconComponent: FileUp,
    path: "/dashboard/case-summarizer"
  },
  {
    title: "FIR & Evidence Analyzer",
    IconComponent: Library,
    path: "/dashboard/fir-analyzer"
  },
  {
    title: "Law Student Hub",
    IconComponent: BookOpen,
    path: "/dashboard/learning-hub"
  },
  {
    title: "FAQ Builder",
    IconComponent: ListChecks,
    path: "/dashboard/faq-builder"
  },
  {
    title: "Vani-Kanoon (Voice)",
    IconComponent: Mic,
    path: "/dashboard/vani-kanoon"
  },
  {
    title: "Settings",
    IconComponent: Settings,
    path: "/dashboard/settings"
  }
];