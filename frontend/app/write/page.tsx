"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { ArrowLeft, Save, Eye, ImagePlus, Code, Link2, Bold, Italic, List, ListOrdered, Quote } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import Link from "next/link"

const categories = [
  "IT 기술 정보",
  "취업 시장 정보",
  "개발자 트렌드",
  "자유 주제",
]

export default function WritePage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const [isPreview, setIsPreview] = useState(false)
  const [formData, setFormData] = useState({
    title: "",
    category: "",
    tags: "",
    content: "",
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)

    // Simulate post creation
    await new Promise((resolve) => setTimeout(resolve, 1000))

    setIsLoading(false)
    router.push("/")
  }

  const insertMarkdown = (type: string) => {
    const textarea = document.getElementById("content") as HTMLTextAreaElement
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const selectedText = formData.content.substring(start, end)
    let insertion = ""

    switch (type) {
      case "bold":
        insertion = `**${selectedText || "굵은 텍스트"}**`
        break
      case "italic":
        insertion = `*${selectedText || "기울임 텍스트"}*`
        break
      case "code":
        insertion = selectedText.includes("\n")
          ? `\`\`\`\n${selectedText || "코드 입력"}\n\`\`\``
          : `\`${selectedText || "코드"}\``
        break
      case "link":
        insertion = `[${selectedText || "링크 텍스트"}](URL)`
        break
      case "image":
        insertion = `![${selectedText || "이미지 설명"}](이미지 URL)`
        break
      case "quote":
        insertion = `> ${selectedText || "인용문"}`
        break
      case "list":
        insertion = `- ${selectedText || "리스트 항목"}`
        break
      case "ordered":
        insertion = `1. ${selectedText || "리스트 항목"}`
        break
    }

    const newContent =
      formData.content.substring(0, start) +
      insertion +
      formData.content.substring(end)
    setFormData({ ...formData, content: newContent })
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-5 w-5" />
            </Button>
          </Link>
          <h1 className="text-2xl font-bold text-foreground">글 쓰기</h1>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            onClick={() => setIsPreview(!isPreview)}
            className="gap-2"
          >
            <Eye className="h-4 w-4" />
            {isPreview ? "편집" : "미리보기"}
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={isLoading || !formData.title || !formData.content}
            className="gap-2 bg-primary text-primary-foreground hover:bg-primary/90"
          >
            <Save className="h-4 w-4" />
            {isLoading ? "저장 중..." : "발행하기"}
          </Button>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Title */}
        <div className="space-y-2">
          <Input
            id="title"
            type="text"
            placeholder="제목을 입력하세요"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            required
            className="border-none bg-transparent text-3xl font-bold placeholder:text-muted-foreground focus-visible:ring-0"
          />
        </div>

        {/* Category & Tags */}
        <div className="flex flex-wrap gap-4">
          <div className="w-full sm:w-48">
            <Label htmlFor="category" className="sr-only">카테고리</Label>
            <Select
              value={formData.category}
              onValueChange={(value) => setFormData({ ...formData, category: value })}
            >
              <SelectTrigger className="bg-secondary">
                <SelectValue placeholder="카테고리 선택" />
              </SelectTrigger>
              <SelectContent>
                {categories.map((category) => (
                  <SelectItem key={category} value={category.toLowerCase()}>
                    {category}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex-1">
            <Label htmlFor="tags" className="sr-only">태그</Label>
            <Input
              id="tags"
              type="text"
              placeholder="태그 입력 (쉼표로 구분)"
              value={formData.tags}
              onChange={(e) => setFormData({ ...formData, tags: e.target.value })}
              className="bg-secondary"
            />
          </div>
        </div>

        {/* Toolbar */}
        <div className="flex flex-wrap items-center gap-1 rounded-lg border border-border bg-secondary p-2">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("bold")}
            title="굵게"
          >
            <Bold className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("italic")}
            title="기울임"
          >
            <Italic className="h-4 w-4" />
          </Button>
          <div className="mx-2 h-6 w-px bg-border" />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("code")}
            title="코드"
          >
            <Code className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("link")}
            title="링크"
          >
            <Link2 className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("image")}
            title="이미지"
          >
            <ImagePlus className="h-4 w-4" />
          </Button>
          <div className="mx-2 h-6 w-px bg-border" />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("quote")}
            title="인용"
          >
            <Quote className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("list")}
            title="목록"
          >
            <List className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => insertMarkdown("ordered")}
            title="번호 목록"
          >
            <ListOrdered className="h-4 w-4" />
          </Button>
        </div>

        {/* Content */}
        {isPreview ? (
          <div className="min-h-[400px] rounded-lg border border-border bg-card p-6">
            <div className="prose prose-invert max-w-none">
              {formData.content ? (
                <div className="whitespace-pre-wrap text-foreground">{formData.content}</div>
              ) : (
                <p className="text-muted-foreground">미리볼 내용이 없습니다.</p>
              )}
            </div>
          </div>
        ) : (
          <div className="space-y-2">
            <Label htmlFor="content" className="sr-only">내용</Label>
            <Textarea
              id="content"
              placeholder="내용을 작성하세요. 마크다운 문법을 지원합니다."
              value={formData.content}
              onChange={(e) => setFormData({ ...formData, content: e.target.value })}
              required
              rows={20}
              className="min-h-[400px] resize-none bg-secondary font-mono text-sm"
            />
          </div>
        )}

        {/* Helper Text */}
        <p className="text-xs text-muted-foreground">
          마크다운 문법을 지원합니다. 코드 블록은 ```언어명 으로 시작하세요.
        </p>
      </form>
    </div>
  )
}
